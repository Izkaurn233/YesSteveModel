package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.api.rendering.event.RenderLayerEvent;
import com.elfmcys.ysm.api.rendering.event.RenderModelEvent;
import com.elfmcys.ysm.client.entity.CustomPlayerEntity;
import java.util.function.BiPredicate;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.capability.VehicleAnimatableCapabilityProvider;
import com.elfmcys.ysm.client.renderer.replace.EntityRendererReplace;
import com.elfmcys.ysm.config.ClientConfig;
import com.elfmcys.ysm.event.api.SpecialPlayerRenderEvent;
import com.elfmcys.ysm.geckolib3.geo.GeoRenderData;
import com.elfmcys.ysm.mixin.client.AvatarRendererAccessor;
import com.elfmcys.ysm.natives.render.NativeRenderer;
import com.elfmcys.ysm.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public class ReplacePlayerRenderEvent {
    public static boolean submit(EntityRenderer<?, ?> renderer, EntityRenderState state, PoseStack poseStack,
        SubmitNodeCollector collector, CameraRenderState camera) {
        if (!RenderUtil.isRenderingLevel() || !(renderer instanceof AvatarRenderer<?> avatarRenderer)
            || !(state instanceof AvatarRenderState avatar) || avatar.isSpectator) {
            return false;
        }
        var input = EntityRendererReplace.getInput(state);
        if (input == null || !(input.entity() instanceof Player player)) {
            return false;
        }
        var localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null || (player == localPlayer
            ? ClientConfig.DISABLE_SELF_MODEL.get() : ClientConfig.DISABLE_OTHER_MODEL.get())) {
            return false;
        }
        var cap = player.getCapability(PlayerAnimatableCapabilityProvider.CAP).orElse(null);
        if (cap == null || !cap.isPresentationAllowed()) {
            return false;
        }
        return cap.consumePrepared(cap.update(input.partialTick()), prepared -> {
            var event = new SpecialPlayerRenderEvent(player, cap, cap.getInstalledModelId());
            boolean suppressed = YesSteveModel.postEvent(event);
            if (!cap.canConsume(prepared, prepared.ctx)) {
                return false;
            }
            if (!suppressed) {
                submitPrepared(cap, prepared, avatarRenderer, avatar, poseStack, collector, camera,
                    event.getTextureLocationOverride(), null);
                if (cap.canConsume(prepared, prepared.ctx)) {
                    EntityRendererReplace.submitVanillaFeatures(renderer, state, poseStack, collector, camera);
                }
            }
            return true;
        });
    }

    public static void submitPrepared(CustomPlayerEntity cap, GeoRenderData data, AvatarRenderer<?> renderer,
        AvatarRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera,
        @Nullable Identifier textureOverride, @Nullable BiPredicate<GeoRenderData, PoseStack> framing) {
        boolean visible = !state.isInvisible && !state.isInvisibleToPlayer;
        if (!visible && !state.appearsGlowing()) {
            return;
        }
        var texture = textureOverride == null ? data.texture : textureOverride;
        var renderType = EntityRendererReplace.renderType(texture, visible, data.modelState.hasTranslucentVertices());
        var player = cap.getEntity();
        int overlay = LivingEntityRenderer.getOverlayCoords(state, 0);
        poseStack.pushPose();
        try {
            setupPose(player, data, renderer, state, poseStack);
            if (framing != null && !framing.test(data, poseStack)) {
                return;
            }
            if (data.renderLayersFirst) {
                submitLayers(cap, data, renderer, state, poseStack, collector, overlay);
            }
            if (cap.canConsume(data, data.ctx)) {
                var event = new RenderModelEvent(player, data, collector, renderType, poseStack, state.lightCoords,
                    overlay, 0xFFFFFFFF);
                if (!YesSteveModel.postEvent(event) && cap.canConsume(data, data.ctx)) {
                    var modelState = data.modelState;
                    NativeRenderer.submit(collector, renderType, poseStack, camera, modelState.getNativeState(),
                        modelState.getVertexCount(), state.lightCoords, overlay, 0xFFFFFFFF, data.ctx.nativeType());
                }
            }
            if (cap.canConsume(data, data.ctx) && !data.renderLayersFirst) {
                submitLayers(cap, data, renderer, state, poseStack, collector, overlay);
            }
            if (cap.canConsume(data, data.ctx)) {
                cap.countRender();
            }
        } finally {
            poseStack.popPose();
        }
    }

    private static void submitLayers(CustomPlayerEntity cap, GeoRenderData data, AvatarRenderer<?> renderer,
        AvatarRenderState state, PoseStack pose, SubmitNodeCollector collector, int overlay) {
        var event = new RenderLayerEvent(cap.getEntity(), data, pose, collector, state.lightCoords, overlay);
        if (!YesSteveModel.postEvent(event) && cap.canConsume(data, data.ctx)) {
            RegisterEntityRenderersEvent.getPlayerRenderer()
                .submitLayers(cap, data, renderer, state, pose, collector, overlay);
        }
    }

    private static void setupPose(Player player, GeoRenderData data, AvatarRenderer<?> renderer,
        AvatarRenderState state, PoseStack poseStack) {
        if (state.pose == Pose.SLEEPING && state.bedOrientation != null) {
            float offset = player.getEyeHeight(Pose.STANDING) - 0.1F;
            poseStack.translate(-state.bedOrientation.getStepX() * offset, 0, -state.bedOrientation.getStepZ() * offset);
        }
        float bodyRot = data.animationData.lerpBodyRot;
        if (player.onClimbable()) {
            bodyRot = player.getLastClimbablePos()
                .flatMap(pos -> player.level().getBlockState(pos).getOptionalValue(HorizontalDirectionalBlock.FACING))
                .map(direction -> direction.getOpposite().get2DDataValue() * 90.0F)
                .orElse(bodyRot);
        }
        var deathTime = state.deathTime;
        var spin = state.isAutoSpinAttack;
        state.deathTime = 0;
        state.isAutoSpinAttack = false;
        try {
            ((AvatarRendererAccessor)renderer).ysm$setupRotations(state, poseStack, bodyRot, state.scale);
        } finally {
            state.deathTime = deathTime;
            state.isAutoSpinAttack = spin;
        }
        var vehicle = player.getVehicle();
        if (vehicle != null) {
            var vehicleCap = VehicleAnimatableCapabilityProvider.CAP.peek(vehicle);
            var rotation = vehicleCap == null ? null : vehicleCap.getRotation();
            if (rotation != null) {
                poseStack.rotate(new Quaternionf().rotateZYX(rotation.z, 0, rotation.x).invert());
            }
        }
        poseStack.translate(0, 0.01F, 0);
        poseStack.scale(data.widthScale, data.heightScale, data.widthScale);
    }
}
