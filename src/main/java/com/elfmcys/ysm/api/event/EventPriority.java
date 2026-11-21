package com.elfmcys.ysm.api.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Handler order on the YSM {@link EventManager}. Lower values run first; unannotated handlers use {@link #NORMAL}.
 * The named constants line up with Forge {@code EventPriority} HIGHEST..LOWEST.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventPriority {
    int HIGHEST = 0;
    int HIGH = 5;
    int NORMAL = 10;
    int LOW = 15;
    int LOWEST = 20;

    int value() default NORMAL;
}
