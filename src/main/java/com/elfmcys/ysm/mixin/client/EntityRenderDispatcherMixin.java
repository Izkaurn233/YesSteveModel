package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.client.event.RegisterEntityRenderersEvent;
import com.elfmcys.ysm.client.event.ReplacePlayerRenderEvent;
import com.elfmcys.ysm.client.renderer.replace.EntityRendererReplace;
import com.elfmcys.ysm.client.renderer.replace.ProjectileRendererReplace;
import com.elfmcys.ysm.config.ClientConfig;
import com.elfmcys.ysm.util.RenderUtil;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Inject(method = "onResourceManagerReload(Lnet/minecraft/server/packs/resources/ResourceManager;)V", at = @At("RETURN"))
    private void ysm$afterRendererReload(ResourceManager manager, CallbackInfo ci,
        @Local EntityRendererProvider.Context context) {
        RegisterEntityRenderersEvent.onReload(context);
    }

    @WrapWithCondition(method = "submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;"
        + "Lnet/minecraft/client/renderer/state/level/CameraRenderState;DDDLcom/mojang/blaze3d/vertex/PoseStack;"
        + "Lnet/minecraft/client/renderer/SubmitNodeCollector;)V",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;"
                + "submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;"
                + "Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;"
                + "Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"))
    private boolean replaceRender(EntityRenderer<?, ?> renderer, EntityRenderState state, PoseStack poseStack,
        SubmitNodeCollector collector, CameraRenderState camera) {
        var input = EntityRendererReplace.getInput(state);
        if (input == null) {
            return true;
        }
        if (input.entity() instanceof Projectile && !ClientConfig.DISABLE_PROJECTILE_MODEL.get()) {
            return !ProjectileRendererReplace.submit(renderer, state, poseStack, collector, camera);
        }
        if (!ClientConfig.DISABLE_VEHICLE_MODEL.get()) {
            RenderUtil.adjustPassengerPosition(input.entity(), poseStack);
            if (EntityRendererReplace.submitVehicle(renderer, state, input, poseStack, collector, camera)) {
                return false;
            }
        }
        return !ReplacePlayerRenderEvent.submit(renderer, state, poseStack, collector, camera);
    }
}
