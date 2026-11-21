package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.client.renderer.replace.ProjectileRendererReplace;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.client.renderer.entity.state.FishingHookRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FishingHookRenderer.class)
public abstract class FishingHookRendererMixin {
    @WrapWithCondition(method = "submit(Lnet/minecraft/client/renderer/entity/state/FishingHookRenderState;"
        + "Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;"
        + "Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitCustomGeometry("
                + "Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;"
                + "Lnet/minecraft/client/renderer/SubmitNodeCollector$CustomGeometryRenderer;)V"))
    private boolean ysm$submitBobber(SubmitNodeCollector collector, PoseStack poseStack, RenderType type,
        SubmitNodeCollector.CustomGeometryRenderer geometry,
        @Local(argsOnly = true) FishingHookRenderState state, @Local(argsOnly = true) CameraRenderState camera) {
        return !ProjectileRendererReplace.submitModel(state, poseStack, collector, camera, true);
    }
}
