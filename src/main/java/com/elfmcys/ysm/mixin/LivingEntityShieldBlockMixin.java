package com.elfmcys.ysm.mixin;

import com.elfmcys.ysm.event.LivingShieldBlockEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityShieldBlockMixin {
    @Inject(method = "applyItemBlocking(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)F",
        at = @At("RETURN"))
    private void ysm$onDamageBlocked(CallbackInfoReturnable<Float> cir) {
        if (cir.getReturnValueF() > 0.0F && (Object)this instanceof ServerPlayer player) {
            LivingShieldBlockEvent.onShieldBlock(player);
        }
    }
}
