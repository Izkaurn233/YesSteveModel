package com.elfmcys.ysm.client.gui;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.client.event.ReplacePlayerRenderEvent;
import com.elfmcys.ysm.config.ClientConfig;
import com.elfmcys.ysm.event.api.SpecialPlayerRenderEvent;
import com.elfmcys.ysm.geckolib3.geo.GeoRenderData;
import com.elfmcys.ysm.mixin.client.GuiGraphicsExtractorAccessor;
import com.elfmcys.ysm.mixin.client.PictureInPictureRendererAccessor;
import com.elfmcys.ysm.util.RenderUtil;
import com.elfmcys.ysm.util.AnimatableCacheUtil;
import java.util.concurrent.ExecutionException;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import net.minecraft.world.entity.player.Player;

/** Captures model nodes while the catalog card still owns its animation and texture borrows. */
public final class GuiModelPreview {
    private GuiModelPreview() {}

    public static void paperDoll(GuiGraphicsExtractor graphics, Player player, double x, double y,
        float scale, float yaw, float partialTick) {
        int width = (int)Math.ceil(scale);
        int height = (int)Math.ceil(scale * 2);
        if (width <= 0 || height <= 0) {
            return;
        }
        int left = (int)Math.round(x);
        int top = (int)Math.round(y);
        var host = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
        if (!(host.createRenderState(player, partialTick) instanceof AvatarRenderState state)) {
            return;
        }
        state.lightCoords = 0xF000F0;
        state.shadowPieces.clear();
        var rotation = new Quaternionf().rotateZ((float)Math.PI).rotateY(
            (float)Math.toRadians(state.bodyRot + yaw - 180));
        var translation = new Vector3f(0, 1, 0);
        int right = left + width;
        int bottom = top + height;
        if (!localPlayer(graphics, player, state, scale, translation, rotation, left, top, right, bottom, true, partialTick)) {
            graphics.entity(state, scale, translation, rotation, null, left, top, right, bottom);
        }
    }

    public static boolean localPlayer(GuiGraphicsExtractor graphics, Player player, AvatarRenderState state, float scale,
        Vector3fc translation, Quaternionfc rotation, int left, int top, int right, int bottom,
        boolean paperDoll, float partialTick) {
        if (!YesSteveModel.isAvailable() || ClientConfig.DISABLE_SELF_MODEL.get() || state.isSpectator
            || player != Minecraft.getInstance().player || right <= left || bottom <= top) {
            return false;
        }
        var cap = player.getCapability(PlayerAnimatableCapabilityProvider.CAP).orElse(null);
        if (cap == null || !cap.isPresentationAllowed()) {
            return false;
        }
        var host = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(state);
        int guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        int width = right - left;
        int height = bottom - top;
        var pose = new PoseStack();
        pose.translate(width * guiScale / 2.0F, height * guiScale / 2.0F, 0);
        float scaled = guiScale * scale;
        pose.scale(scaled, scaled, -scaled);
        pose.translate(translation.x(), translation.y(), translation.z());
        pose.rotate(rotation);
        var camera = new CameraRenderState();
        var projection = new Projection();
        projection.setupOrtho(-1000, 1000, width * guiScale, height * guiScale, true);
        projection.getMatrix(camera.projectionMatrix);
        var storage = new SubmitNodeStorage();
        var previous = RenderUtil.extractRenderContext();
        RenderUtil.setRenderingInInventory(!paperDoll);
        RenderUtil.setRenderingInPaperDoll(paperDoll);
        try {
            if (!cap.consumePrepared(cap.update(partialTick), data -> {
                var event = new SpecialPlayerRenderEvent(player, cap, cap.getInstalledModelId());
                boolean cancel = YesSteveModel.postEvent(event);
                if (!cap.canConsume(data, data.ctx)) {
                    return false;
                }
                if (!cancel) {
                    ReplacePlayerRenderEvent.submitPrepared(cap, data, host, state, pose, storage, camera,
                        event.getTextureLocationOverride(), null);
                }
                return true;
            })) {
                return false;
            }
        } finally {
            RenderUtil.setRenderingInInventory(previous.inventory());
            RenderUtil.setRenderingInPaperDoll(previous.paperDoll());
        }
        submit(graphics, left, top, width, height, storage);
        return true;
    }

    public static void prepared(GuiGraphicsExtractor graphics, int left, int top, int width, int height,
        float modelY, float scale, CustomGuiPlayerEntity entity, @Nullable GuiFrameInput input,
        @Nullable GeoRenderData data) {
        if (input != null && data != null) {
            extract(graphics, left, top, width, height, modelY, scale, entity, input, data, null);
        }
    }

    public static void framed(GuiGraphicsExtractor graphics, int left, int top, int width, int height,
        float partialTick, CustomGuiPlayerEntity entity, GuiModelFraming framing) {
        var input = GuiFrameInput.capture(entity.getEntity(), partialTick, false, true, false, false, 0);
        extract(graphics, left, top, width, height, top + height / 2.0F, 1, entity, input, null, framing);
    }

    public static void textureScene(GuiGraphicsExtractor graphics, int left, int top, int width, int height,
        float anchorX, float anchorY, float scale, float pitch, float yaw, float partialTick,
        CustomGuiPlayerEntity entity, boolean ground) {
        if (width <= 0 || height <= 0) {
            return;
        }
        var input = GuiFrameInput.capture(entity.getEntity(), partialTick, false, false, false, false, 0);
        var scene = new Scene(anchorX - left, pitch, yaw, ground);
        int guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        var nodes = capture(entity, input, null, null, width * guiScale, height * guiScale,
            (anchorY - top) * guiScale, scale, guiScale, scene);
        if (nodes != null) {
            submit(graphics, left, top, width, height, nodes);
        }
    }

    private static void extract(GuiGraphicsExtractor graphics, int left, int top, int width, int height,
        float modelY, float scale, CustomGuiPlayerEntity entity, GuiFrameInput input,
        @Nullable GeoRenderData prepared, @Nullable GuiModelFraming framing) {
        if (width <= 0 || height <= 0 || !entity.isPresentationAllowed()) {
            return;
        }
        int guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        var nodes = capture(entity, input, prepared, framing, width * guiScale, height * guiScale,
            (modelY - top) * guiScale, scale, guiScale);
        if (nodes == null) {
            return;
        }
        submit(graphics, left, top, width, height, nodes);
    }

    private static void submit(GuiGraphicsExtractor graphics, int left, int top, int width, int height,
        SubmitNodeStorage nodes) {
        var clip = new ScreenRectangle(left, top, width, height);
        var screenPose = new Matrix3x2f(graphics.pose());
        ((GuiGraphicsExtractorAccessor) graphics).ysm$getGuiRenderState().addPicturesInPictureState(
            new State(nodes, left, top, left + width, top + height, screenPose, clip, clip.transformMaxBounds(screenPose)));
    }

    static @Nullable SubmitNodeStorage capture(CustomGuiPlayerEntity entity, GuiFrameInput input,
        @Nullable GeoRenderData prepared, @Nullable GuiModelFraming framing, int canvasWidth, int canvasHeight,
        float modelY, float scale, float guiScale) {
        return capture(entity, input, prepared, framing, canvasWidth, canvasHeight, modelY, scale, guiScale, null);
    }

    private static @Nullable SubmitNodeStorage capture(CustomGuiPlayerEntity entity, GuiFrameInput input,
        @Nullable GeoRenderData prepared, @Nullable GuiModelFraming framing, int canvasWidth, int canvasHeight,
        float modelY, float scale, float guiScale, @Nullable Scene scene) {
        var minecraft = Minecraft.getInstance();
        var hostRenderer = minecraft.getEntityRenderDispatcher().getRenderer(entity.getEntity());
        if (!(hostRenderer instanceof AvatarRenderer<?> avatarRenderer)) {
            return null;
        }

        var projection = new Projection();
        projection.setupOrtho(-1000.0F, 1000.0F, canvasWidth, canvasHeight, true);
        var camera = new CameraRenderState();
        projection.getMatrix(camera.projectionMatrix);

        var pose = new PoseStack();
        pose.translate(scene == null ? canvasWidth / 2.0F : scene.x() * guiScale,
            modelY + (input.disableRotation() ? 5.5F * guiScale : 0), 0);
        float modelScale = scale * guiScale;
        pose.scale(modelScale, modelScale, -modelScale);
        if (scene != null) {
            pose.translate(0, 0.8F, 0);
        }
        pose.rotateDegrees(Axis.ZP, 180.0F);
        pose.rotateDegrees(Axis.XP, scene == null ? (input.disableRotation() ? 0 : -10) : -10 + scene.pitch());
        pose.rotateDegrees(Axis.YP, input.vehicleRotation());

        var nodes = new SubmitNodeStorage();
        var wasInventory = RenderUtil.extractRenderContext().inventory();
        RenderUtil.setRenderingInInventory(true);
        var living = entity.getEntity();
        var oldPose = living.getPose();
        try (var applied = input.apply()) {
            if (scene != null) {
                scene.prepare(minecraft, entity, input.partialTick(), pose, camera, nodes);
            }
            var data = prepared == null ? entity.update(input.partialTick()) : prepared;
            var state = hostRenderer.createRenderState(entity.getEntity(), input.partialTick());
            if (!(state instanceof AvatarRenderState avatar)) {
                return null;
            }
            avatar.lightCoords = 0xF000F0;
            if (!entity.consumePrepared(data, current -> {
                ReplacePlayerRenderEvent.submitPrepared(entity, current, avatarRenderer, avatar, pose, nodes, camera,
                    null, framing == null ? null : (borrowed, drawPose) ->
                        framing.applyPreview(borrowed.installation, borrowed.modelState, drawPose, canvasWidth, canvasHeight));
                return true;
            })) {
                return null;
            }
        } finally {
            try {
                living.setPose(oldPose);
            } finally {
                RenderUtil.setRenderingInInventory(wasInventory);
            }
        }

        return nodes;
    }

    private record Scene(float x, float pitch, float yaw, boolean ground) {
        private void prepare(Minecraft minecraft, CustomGuiPlayerEntity preview, float partialTick,
            PoseStack pose, CameraRenderState camera, SubmitNodeStorage nodes) {
            var player = preview.getEntity();
            player.yBodyRot = player.yBodyRotO = -yaw;
            player.setYRot(180);
            player.yRotO = 180;
            player.setXRot(0);
            player.xRotO = 0;
            player.yHeadRot = player.yHeadRotO = -yaw;

            var animation = preview.getPreviewInfo();
            boolean sleeping = animation.hasPreview("sleep");
            if (sleeping || ground) {
                var resolver = new BlockModelResolver(minecraft.getModelManager());
                if (sleeping) {
                    pose.pushPose();
                    try {
                        pose.rotateDegrees(Axis.YP, yaw + 180);
                        pose.translate(-0.5, 0, 0.5);
                        block(resolver, Blocks.BED.red()).submit(pose, nodes, 0xF000F0, OverlayTexture.NO_OVERLAY, 0);
                    } finally {
                        pose.popPose();
                    }
                }
                if (ground) {
                    submitGround(resolver, pose, nodes);
                }
            }

            if (sleeping) {
                pose.rotateDegrees(Axis.YP, yaw - 90);
                pose.translate(0.5, 0.5625, 0);
                player.setPose(Pose.SLEEPING);
            }
            if (animation.hasPreview("swim") || animation.hasPreview("swim_stand")) {
                player.setPose(Pose.SWIMMING);
            }
            if (animation.hasPreview("sneak") || animation.hasPreview("sneaking")) {
                player.setPose(Pose.CROUCHING);
            }
            if (animation.hasPreview("sit")) {
                pose.translate(0, -0.5, 0);
            }
            if (animation.hasPreview("ride")) {
                pose.translate(0, 0.85, 0);
                submitMount(minecraft, preview, EntityTypes.HORSE, partialTick, pose, camera, nodes);
            } else if (animation.hasPreview("ride_pig")) {
                pose.translate(0, 0.3125, 0);
                submitMount(minecraft, preview, EntityTypes.PIG, partialTick, pose, camera, nodes);
            } else if (animation.hasPreview("boat")) {
                pose.translate(0, -0.45, 0);
                submitMount(minecraft, preview, EntityTypes.OAK_BOAT, partialTick, pose, camera, nodes);
            }
        }

        private void submitGround(BlockModelResolver resolver, PoseStack pose, SubmitNodeStorage nodes) {
            pose.pushPose();
            try {
                pose.rotateDegrees(Axis.YP, yaw);
                pose.translate(-1.5, -1, -2.5);
                var grass = block(resolver, Blocks.GRASS_BLOCK);
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        pose.translate(0, 0, 1);
                        grass.submit(pose, nodes, 0xF000F0, OverlayTexture.NO_OVERLAY, 0);
                    }
                    pose.translate(1, 0, -3);
                }
                pose.translate(-1, 1, 1);
                block(resolver, Blocks.SHORT_GRASS).submit(pose, nodes, 0xF000F0, OverlayTexture.NO_OVERLAY, 0);
                pose.translate(0, 0, 1);
                block(resolver, Blocks.RED_TULIP).submit(pose, nodes, 0xF000F0, OverlayTexture.NO_OVERLAY, 0);
            } finally {
                pose.popPose();
            }
        }

        private static BlockModelRenderState block(BlockModelResolver resolver, Block block) {
            var state = new BlockModelRenderState();
            resolver.update(state, block.defaultBlockState(), BlockDisplayContext.create());
            return state;
        }

        private void submitMount(Minecraft minecraft, CustomGuiPlayerEntity preview, EntityType<?> type,
            float partialTick, PoseStack pose, CameraRenderState camera, SubmitNodeStorage nodes) {
            if (minecraft.level == null) {
                return;
            }
            var player = preview.getEntity();
            try {
                var mount = AnimatableCacheUtil.ENTITIES_CACHE.get(EntityType.getKey(type),
                    () -> type.create(player.level(), EntitySpawnReason.COMMAND));
                var dispatcher = minecraft.getEntityRenderDispatcher();
                var state = dispatcher.getRenderer(mount).createRenderState(mount, partialTick);
                state.lightCoords = 0xF000F0;
                state.shadowPieces.clear();
                double y = mount.getY() - mount.getPassengerRidingPosition(player).y
                    + player.getVehicleAttachmentPoint(mount).y;
                pose.pushPose();
                try {
                    pose.rotateDegrees(Axis.YP, yaw);
                    dispatcher.submit(state, camera, 0, y, 0, pose, nodes);
                } finally {
                    pose.popPose();
                }
            } catch (ExecutionException failure) {
                throw new IllegalStateException("Failed to prepare the preview vehicle", failure);
            }
        }
    }

    public record State(SubmitNodeStorage nodes, int x0, int y0, int x1, int y1, Matrix3x2f pose,
                        ScreenRectangle scissorArea, ScreenRectangle bounds) implements PictureInPictureRenderState {
        @Override
        public float scale() {
            return 1;
        }
    }

    public static final class Renderer extends PictureInPictureRenderer<State> {
        @Override
        public void prepare(State state, GuiRenderState gui, FeatureRenderDispatcher features, int guiScale) {
            var accessor = (PictureInPictureRendererAccessor) (Object) this;
            var original = accessor.ysm$getSubmitNodeStorage();
            accessor.ysm$setSubmitNodeStorage(state.nodes());
            try {
                super.prepare(state, gui, features, guiScale);
            } finally {
                accessor.ysm$setSubmitNodeStorage(original);
            }
        }

        @Override
        public Class<State> getRenderStateClass() {
            return State.class;
        }

        @Override
        protected void renderToTexture(State state, PoseStack pose, SubmitNodeCollector collector) {
            Minecraft.getInstance().gameRenderer.lighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
        }

        @Override
        protected String getTextureLabel() {
            return "YSM model preview";
        }
    }
}
