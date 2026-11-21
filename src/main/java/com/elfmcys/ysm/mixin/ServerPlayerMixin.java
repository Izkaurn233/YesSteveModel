package com.elfmcys.ysm.mixin;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.event.CapabilityEvent;
import com.elfmcys.ysm.event.MobEffectSyncEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;positionRider(Lnet/minecraft/world/entity/Entity;)V",
            shift = At.Shift.AFTER))
    private void afterPositionRider(Entity vehicle, boolean force, boolean sendEventAndTriggers,
        CallbackInfoReturnable<Boolean> cir) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)(Object)this;
        if (vehicle.getFirstPassenger() == serverPlayer) {
            CapabilityEvent.onVehicleSetModel(vehicle, serverPlayer);
        }
    }

    @Inject(method = "onEffectAdded(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)V",
        at = @At("RETURN"))
    private void ysm$afterEffectAdded(MobEffectInstance effect, Entity source, CallbackInfo ci) {
        MobEffectSyncEvent.onAddedOrUpdated((ServerPlayer)(Object)this, effect);
    }

    @Inject(method = "onEffectUpdated(Lnet/minecraft/world/effect/MobEffectInstance;ZLnet/minecraft/world/entity/Entity;)V",
        at = @At("RETURN"))
    private void ysm$afterEffectUpdated(MobEffectInstance effect, boolean reapplyAttributes, Entity source, CallbackInfo ci) {
        if (reapplyAttributes) {
            MobEffectSyncEvent.onAddedOrUpdated((ServerPlayer)(Object)this, effect);
        }
    }

    @Inject(method = "onEffectsRemoved(Ljava/util/Collection;)V", at = @At("RETURN"))
    private void ysm$afterEffectsRemoved(Collection<MobEffectInstance> effects, CallbackInfo ci) {
        MobEffectSyncEvent.onRemoved((ServerPlayer)(Object)this, effects);
    }
}
