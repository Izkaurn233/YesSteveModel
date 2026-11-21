package com.elfmcys.ysm.client.renderer.replace;

import com.elfmcys.ysm.capability.VehicleAnimatableCapabilityProvider;
import com.elfmcys.ysm.geckolib3.geo.CustomTranslucentRenderType;
import com.elfmcys.ysm.mixin.client.EntityRendererAccessor;
import com.elfmcys.ysm.natives.render.NativeRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class EntityRendererReplace {
    private static final RenderStateDataKey<RenderInput> RENDER_INPUT = RenderStateDataKey.create();

    public record RenderInput(Entity entity, float partialTick) {}

    public static void bind(EntityRenderState state, Entity entity, float partialTick) {
        ((FabricRenderState)state).setData(RENDER_INPUT, new RenderInput(entity, partialTick));
    }

    public static @Nullable RenderInput getInput(EntityRenderState state) {
        return ((FabricRenderState)state).getData(RENDER_INPUT);
    }

    public static void submitVanillaFeatures(EntityRenderer<?, ?> renderer, EntityRenderState state,
        PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.leashStates != null) {
            for (var leash : state.leashStates) {
                collector.submitLeash(poseStack, leash);
            }
        }
        ((EntityRendererAccessor)renderer).ysm$submitNameDisplay(state, poseStack, collector, camera);
    }

    public static RenderType renderType(Identifier texture, boolean visible, boolean translucent) {
        if (!visible) {
            return RenderTypes.outline(texture);
        }
        return translucent ? CustomTranslucentRenderType.create(texture) : RenderTypes.entityCutout(texture);
    }

    public static boolean submitVehicle(EntityRenderer<?, ?> renderer, EntityRenderState state, RenderInput input,
        PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        var entity = input.entity();
        var cap = VehicleAnimatableCapabilityProvider.CAP.peek(entity);
        var player = Minecraft.getInstance().player;
        if (cap == null || player == null || !cap.isPresentationAllowed()) {
            return false;
        }
        return cap.consumePrepared(cap.update(input.partialTick()), data -> {
            boolean visible = !entity.isInvisibleTo(player);
            if (visible || state.appearsGlowing()) {
                var type = renderType(data.texture, visible, data.modelState.hasTranslucentVertices());
                poseStack.pushPose();
                try {
                    poseStack.rotateDegrees(Axis.YP, 180.0F - getYaw(state, input));
                    poseStack.scale(data.widthScale, data.heightScale, data.widthScale);
                    var modelState = data.modelState;
                    if (!NativeRenderer.submit(collector, type, poseStack, camera, modelState.getNativeState(),
                        modelState.getVertexCount(), state.lightCoords, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF,
                        data.ctx.nativeType())) {
                        return false;
                    }
                    cap.countRender();
                } finally {
                    poseStack.popPose();
                }
            }
            submitVanillaFeatures(renderer, state, poseStack, collector, camera);
            return true;
        });
    }

    private static float getYaw(EntityRenderState state, RenderInput input) {
        return switch (state) {
            case MinecartRenderState cart -> getMinecartYaw(cart);
            case BoatRenderState boat -> boat.yRot;
            case LivingEntityRenderState living -> living.bodyRot;
            default -> input.entity().getYRot(input.partialTick());
        };
    }

    private static float getMinecartYaw(MinecartRenderState cart) {
        if (!cart.isNewRender && cart.frontPos != null && cart.backPos != null) {
            var direction = cart.backPos.subtract(cart.frontPos);
            if (direction.length() != 0) {
                return (float)Math.toDegrees(Math.atan2(direction.z, direction.x));
            }
        }
        return cart.yRot;
    }
}
