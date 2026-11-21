package com.elfmcys.ysm.mixin.client;

import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("equipment")
    EntityEquipment ysm$getEquipment();

    @Accessor("swingState")
    LivingEntity.SwingState ysm$getSwingState();

    @Invoker("setLivingEntityFlag")
    void setFlag(int pKey, boolean pValue);
}
