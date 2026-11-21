package com.elfmcys.ysm.api.event;

/** An event whose default action a handler can suppress. Replaces Forge {@code @Cancelable}. */
public interface Cancellable {
    boolean isCancelled();

    void setCancelled(boolean cancelled);
}
