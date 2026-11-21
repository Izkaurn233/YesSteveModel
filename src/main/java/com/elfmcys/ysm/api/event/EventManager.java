package com.elfmcys.ysm.api.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Event bus for YSM's public events. It replaces the Forge mod bus that YSM used to post its API events on.
 * <p>
 * Handlers are {@link EventTarget} methods with exactly one {@link Event} parameter, ordered by
 * {@link EventPriority} (lower first, stable for equal priority). Dispatch matches the exact runtime class of the
 * event. Each event type keeps an immutable handler array that is replaced on registration (copy-on-write), so
 * {@link #post(Event)} takes no lock and may run on any thread that the posting owner allows.
 * <p>
 * Failure: a handler exception is logged and does not stop the remaining handlers or reach the poster. This matches
 * YSM's ordinary-failure isolation for optional integrations.
 */
public final class EventManager {
    private static final Logger LOGGER = LogManager.getLogger("ysm-events");
    private static final Handler[] EMPTY_HANDLERS = new Handler[0];

    private final Map<Class<? extends Event>, Handler[]> handlerMap = new ConcurrentHashMap<>();

    /** Registers the instance {@link EventTarget} methods of {@code owner} and its superclasses. */
    public void register(Object owner) {
        if (owner == null) {
            return;
        }
        if (owner instanceof Class<?> type) {
            register(type);
            return;
        }
        scan(owner.getClass(), owner, false);
    }

    /** Registers the static {@link EventTarget} methods of {@code type} and its superclasses. Replaces Forge {@code register(Class)}. */
    public void register(Class<?> type) {
        if (type != null) {
            scan(type, type, true);
        }
    }

    private void scan(Class<?> start, Object owner, boolean statics) {
        for (Class<?> clazz = start; clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Method method : clazz.getDeclaredMethods()) {
                EventTarget target = method.getAnnotation(EventTarget.class);
                if (target == null || Modifier.isStatic(method.getModifiers()) != statics) {
                    continue;
                }
                if (method.getParameterCount() != 1 || method.getReturnType() != void.class) {
                    LOGGER.warn("Ignoring @EventTarget {}: needs one event parameter and a void return", method);
                    continue;
                }
                Class<?> param = method.getParameterTypes()[0];
                if (!Event.class.isAssignableFrom(param)) {
                    LOGGER.warn("Ignoring @EventTarget {}: parameter {} is not a YSM Event", method, param.getName());
                    continue;
                }
                Class<? extends Event> eventClass = param.asSubclass(Event.class);
                EventPriority priority = method.getAnnotation(EventPriority.class);
                Consumer<Event> consumer = buildConsumer(method, statics ? null : owner, param);
                if (consumer == null) {
                    continue;
                }
                Handler handler = new Handler(owner, method, consumer,
                    priority != null ? priority.value() : EventPriority.NORMAL, target.receiveCancelled());
                handlerMap.compute(eventClass, (key, current) -> insertHandler(current, handler));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Consumer<Event> buildConsumer(Method method, Object instance, Class<?> param) {
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(method.getDeclaringClass(), MethodHandles.lookup());
            MethodHandle handle = lookup.unreflect(method);
            MethodType factoryType = instance == null
                ? MethodType.methodType(Consumer.class)
                : MethodType.methodType(Consumer.class, method.getDeclaringClass());
            CallSite site = LambdaMetafactory.metafactory(lookup, "accept", factoryType,
                MethodType.methodType(void.class, Object.class), handle, MethodType.methodType(void.class, param));
            return (Consumer<Event>)(instance == null ? site.getTarget().invoke() : site.getTarget().invoke(instance));
        } catch (Throwable metafactoryFailure) {
            // Fallback for lookups the metafactory rejects (for example hidden or cross-loader classes).
            try {
                method.setAccessible(true);
                MethodHandle bound = MethodHandles.lookup().unreflect(method);
                if (instance != null) {
                    bound = bound.bindTo(instance);
                }
                MethodHandle invoker = bound.asType(MethodType.methodType(void.class, Event.class));
                return event -> {
                    try {
                        invoker.invokeExact(event);
                    } catch (Throwable throwable) {
                        sneakyThrow(throwable);
                    }
                };
            } catch (ReflectiveOperationException | RuntimeException exception) {
                exception.addSuppressed(metafactoryFailure);
                LOGGER.error("Failed to bind event handler {}", method, exception);
                return null;
            }
        }
    }

    /** Adds a persistent listener. Close or {@link Listener#unregister()} the result to remove it. */
    public <E extends Event> Listener<E> addListener(Class<E> eventClass, Consumer<E> consumer) {
        return addListener(eventClass, EventPriority.NORMAL, consumer);
    }

    public <E extends Event> Listener<E> addListener(Class<E> eventClass, int priority, Consumer<E> consumer) {
        return registerListener(eventClass, priority, consumer, false);
    }

    /** Adds a listener that removes itself after its first dispatch. */
    public <E extends Event> Listener<E> addOnceListener(Class<E> eventClass, Consumer<E> consumer) {
        return registerListener(eventClass, EventPriority.NORMAL, consumer, true);
    }

    private <E extends Event> Listener<E> registerListener(Class<E> eventClass, int priority, Consumer<E> consumer,
        boolean once) {
        Objects.requireNonNull(eventClass, "eventClass");
        Objects.requireNonNull(consumer, "consumer");
        Listener<E> listener = new Listener<>(this, eventClass, consumer, once);
        Handler handler = new Handler(listener, null, event -> listener.dispatch(eventClass.cast(event)), priority,
            false);
        handlerMap.compute(eventClass, (key, current) -> insertHandler(current, handler));
        return listener;
    }

    void unregister(Listener<?> listener) {
        handlerMap.computeIfPresent(listener.eventClassRaw(), (key, handlers) -> removeWhere(handlers,
            handler -> handler.owner != listener));
    }

    /** Removes every handler registered through {@code owner}, an instance or a {@code Class} of static handlers. */
    public void unregister(Object owner) {
        if (owner == null) {
            return;
        }
        for (Class<? extends Event> eventClass : handlerMap.keySet()) {
            handlerMap.computeIfPresent(eventClass, (key, handlers) -> removeWhere(handlers,
                handler -> handler.owner != owner));
        }
    }

    public boolean hasListeners(Class<? extends Event> eventClass) {
        Handler[] handlers = handlerMap.get(eventClass);
        return handlers != null && handlers.length != 0;
    }

    /**
     * Dispatches {@code event} to its handlers in priority order.
     *
     * @return {@code true} when the event is {@link Cancellable} and was cancelled, the same contract as Forge
     *     {@code IEventBus.post}.
     */
    public boolean post(Event event) {
        Handler[] handlers = handlerMap.get(event.getClass());
        Cancellable cancellable = event instanceof Cancellable c ? c : null;
        if (handlers != null) {
            for (Handler handler : handlers) {
                if (cancellable != null && cancellable.isCancelled() && !handler.receiveCancelled) {
                    continue;
                }
                try {
                    handler.consumer.accept(event);
                } catch (Throwable throwable) {
                    LOGGER.error("Event handler {} failed on {}", handler.describe(), event.getClass().getName(),
                        throwable);
                }
            }
        }
        return cancellable != null && cancellable.isCancelled();
    }

    private static Handler[] insertHandler(Handler[] current, Handler handler) {
        Handler[] handlers = current == null ? EMPTY_HANDLERS : current;
        for (Handler existing : handlers) {
            if (existing.owner == handler.owner && existing.method != null
                && Objects.equals(existing.method, handler.method)) {
                return handlers;
            }
        }
        int index = 0;
        while (index < handlers.length && handlers[index].priority <= handler.priority) {
            index++;
        }
        Handler[] updated = new Handler[handlers.length + 1];
        System.arraycopy(handlers, 0, updated, 0, index);
        updated[index] = handler;
        System.arraycopy(handlers, index, updated, index + 1, handlers.length - index);
        return updated;
    }

    private static Handler[] removeWhere(Handler[] handlers, Predicate<Handler> keep) {
        int remaining = 0;
        for (Handler handler : handlers) {
            if (keep.test(handler)) {
                remaining++;
            }
        }
        if (remaining == handlers.length) {
            return handlers;
        }
        if (remaining == 0) {
            return null;
        }
        Handler[] updated = new Handler[remaining];
        int index = 0;
        for (Handler handler : handlers) {
            if (keep.test(handler)) {
                updated[index++] = handler;
            }
        }
        return updated;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow(Throwable throwable) throws T {
        throw (T)throwable;
    }

    private record Handler(Object owner, Method method, Consumer<Event> consumer, int priority,
        boolean receiveCancelled) {
        String describe() {
            return method != null ? method.toString() : String.valueOf(owner);
        }
    }
}
