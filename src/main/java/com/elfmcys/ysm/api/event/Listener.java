package com.elfmcys.ysm.api.event;

import java.util.function.Consumer;

/** A lambda listener registered through {@link EventManager#addListener}. Closing it unregisters it. */
public final class Listener<E extends Event> implements AutoCloseable {
    private final EventManager manager;
    private final Class<E> eventClass;
    private final Consumer<E> consumer;
    private final boolean once;
    private volatile boolean registered = true;

    Listener(EventManager manager, Class<E> eventClass, Consumer<E> consumer, boolean once) {
        this.manager = manager;
        this.eventClass = eventClass;
        this.consumer = consumer;
        this.once = once;
    }

    Class<E> eventClassRaw() {
        return eventClass;
    }

    void dispatch(E event) {
        if (!registered) {
            return;
        }
        try {
            consumer.accept(event);
        } finally {
            if (once) {
                unregister();
            }
        }
    }

    public Class<E> getEventClass() {
        return eventClass;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void unregister() {
        if (!registered) {
            return;
        }
        registered = false;
        manager.unregister(this);
    }

    @Override
    public void close() {
        unregister();
    }
}
