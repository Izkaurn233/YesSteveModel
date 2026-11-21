package com.elfmcys.ysm.client.renderer.replace;

import com.elfmcys.ysm.capability.ProjectileAnimatableCapabilityProvider;
import com.elfmcys.ysm.natives.render.NativeRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.FishingHookRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.Projectile;

public class ProjectileRendererReplace {
    public static boolean submit(EntityRenderer<?, ?> renderer, EntityRenderState state, PoseStack poseStack,
        SubmitNodeCollector collector, CameraRenderState camera) {
        if (state instanceof FishingHookRenderState || !submitModel(state, poseStack, collector, camera, false)) {
            return false;
        }
        EntityRendererReplace.submitVanillaFeatures(renderer, state, poseStack, collector, camera);
        return true;
    }

    public static boolean submitModel(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
        CameraRenderState camera, boolean fishingHook) {
        var frame = EntityRendererReplace.getInput(state);
        if (frame == null || !(frame.entity() instanceof Projectile projectile)) {
            return false;
        }
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }
        var animatable = projectile.getCapability(ProjectileAnimatableCapabilityProvider.CAP).orElse(null);
        if (animatable == null || !animatable.isPresentationAllowed()) {
            return false;
        }

        var data = animatable.update(frame.partialTick());
        return animatable.consumePrepared(data, prepared -> {
            var visible = !projectile.isInvisibleTo(player);
            var glowing = state.appearsGlowing();
            if (visible || glowing) {
                var renderType = EntityRendererReplace.renderType(prepared.texture, visible,
                    prepared.modelState.hasTranslucentVertices());
                PoseStack.Pose bobberPose = null;
                if (fishingHook) {
                    // The vanilla bobber subpass has already applied billboard rotation and scale.
                    bobberPose = poseStack.last().copy();
                    poseStack.popPose();
                }
                poseStack.pushPose();
                try {
                    poseStack.rotateDegrees(Axis.YP, Mth.lerp(frame.partialTick(), projectile.yRotO, projectile.getYRot()) - 90);
                    if (!fishingHook) {
                        poseStack.rotateDegrees(Axis.ZP, Mth.lerp(frame.partialTick(), projectile.xRotO, projectile.getXRot()));
                    }
                    poseStack.scale(prepared.widthScale, prepared.heightScale, prepared.widthScale);
                    var modelState = prepared.modelState;
                    if (!NativeRenderer.submit(collector, renderType, poseStack, camera, modelState.getNativeState(),
                        modelState.getVertexCount(), state.lightCoords, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF,
                        prepared.ctx.nativeType())) {
                        return false;
                    }
                    animatable.countRender();
                } finally {
                    poseStack.popPose();
                    if (bobberPose != null) {
                        poseStack.pushPose();
                        poseStack.last().set(bobberPose);
                    }
                }
            }
            return true;
        });
    }
}
