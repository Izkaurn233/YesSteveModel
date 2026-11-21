package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.VehicleAnimatableCapabilityProvider;
import com.elfmcys.ysm.client.renderer.replace.EntityRendererReplace;
import com.elfmcys.ysm.config.ClientConfig;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererStateMixin {
    @Inject(method = "createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;",
        at = @At("RETURN"))
    private void ysm$bindEntity(Entity entity, float partialTick, CallbackInfoReturnable<EntityRenderState> cir) {
        if (YesSteveModel.isAvailable() && (entity instanceof Player
            || (entity instanceof Projectile && !ClientConfig.DISABLE_PROJECTILE_MODEL.get())
            || (!ClientConfig.DISABLE_VEHICLE_MODEL.get()
                && (entity.isPassenger() || VehicleAnimatableCapabilityProvider.CAP.peek(entity) != null)))) {
            EntityRendererReplace.bind(cir.getReturnValue(), entity, partialTick);
        }
    }
}
