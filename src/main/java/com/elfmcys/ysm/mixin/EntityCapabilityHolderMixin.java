package com.elfmcys.ysm.mixin;

import com.elfmcys.ysm.platform.capability.YsmCapabilityHolder;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

/**
 * PORT(26.3-fabric): makes every {@link Entity} a {@link YsmCapabilityHolder}; the interface's default method does
 * the work. The compile-time view comes from Loom interface injection declared in fabric.mod.json.
 */
@Mixin(Entity.class)
public abstract class EntityCapabilityHolderMixin implements YsmCapabilityHolder {
}
