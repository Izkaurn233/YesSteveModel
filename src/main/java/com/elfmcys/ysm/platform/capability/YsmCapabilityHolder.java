package com.elfmcys.ysm.platform.capability;

import net.minecraft.world.entity.Entity;

/**
 * Injected into {@link Entity} through Loom interface injection (fabric.mod.json {@code loom:injected_interfaces})
 * and applied at runtime by {@code mixin.EntityCapabilityHolderMixin}, so the existing
 * {@code entity.getCapability(XxxProvider.CAP)} call sites keep compiling.
 */
public interface YsmCapabilityHolder {
    default <T> CapOptional<T> getCapability(CapKey<T> key) {
        return key.get((Entity)this);
    }
}
