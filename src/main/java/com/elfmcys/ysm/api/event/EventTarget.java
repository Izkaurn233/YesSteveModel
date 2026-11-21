package com.elfmcys.ysm.api.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a one-parameter method as a handler for the event type of its parameter. Replaces Forge {@code @SubscribeEvent}.
 * Instance methods are bound through {@link EventManager#register(Object)}, static methods through
 * {@link EventManager#register(Class)}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventTarget {
    /**
     * Whether the handler still runs after an earlier handler cancelled the event. Matches Forge
     * {@code receiveCanceled}, which defaults to false.
     */
    boolean receiveCancelled() default false;
}
