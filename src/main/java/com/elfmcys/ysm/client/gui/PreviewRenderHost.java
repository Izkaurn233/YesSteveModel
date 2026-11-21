package com.elfmcys.ysm.client.gui;

import com.elfmcys.ysm.client.animation.AnimationRegister;
import com.elfmcys.ysm.model.resource.client.AcquireResult;
import com.elfmcys.ysm.model.resource.client.ModelRenderTarget;
import com.elfmcys.ysm.model.resource.client.ModelResourcePurpose;
import com.elfmcys.ysm.model.resource.client.ResourceLease;
import com.elfmcys.ysm.model.resource.client.ResourceRequest;
import com.elfmcys.ysm.proto.mixel.manifest.info.SettingsProto;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.textures.GpuTexture;
import java.util.Optional;
import java.util.OptionalDouble;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.joml.Vector4f;

/** Draws one already-published player target into a private host framebuffer. */
public final class PreviewRenderHost {
    public static final int WIDTH = 156;
    public static final int HEIGHT = 270;
    private static final float GUI_SCALE = 3.0F;

    private PreviewRenderHost() {}

    public static NativeImage render(ResourceRequest request, ResourceLease lease) {
        RenderSystem.assertOnRenderThread();
        var acquired = lease.poll();
        if (!(acquired instanceof AcquireResult.Ready ready)) {
            throw new IllegalArgumentException("Preview rendering requires a ready target");
        }
        var target = ready.target();
        if (target.playerResources() == null) {
            throw new IllegalArgumentException("Preview rendering requires a player target");
        }

        var entity = new CustomGuiPlayerEntity();
        try {
            entity.installPreviewResource(request, new BorrowedTargetLease(request, target));
            entity.getPreviewInfo().setPreview(AnimationRegister.IDLE);
            entity.getPreviewInfo().setHover(AnimationRegister.EMPTY);
            entity.getPreviewInfo().setFocus(AnimationRegister.EMPTY);
            return renderEntity(entity, target);
        } finally {
            entity.reset();
        }
    }

    private static NativeImage renderEntity(CustomGuiPlayerEntity entity, ModelRenderTarget target) {
        var minecraft = Minecraft.getInstance();
        var input = GuiFrameInput.capture(entity.getEntity(),
            minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true),
            disablePreviewRotation(target.info().getSettings()), true, false, false, 0);
        var nodes = GuiModelPreview.capture(entity, input, null, null, WIDTH, HEIGHT,
            HEIGHT / 2.0F + 20.0F * GUI_SCALE, 30.0F, GUI_SCALE);
        if (nodes == null) {
            throw new IllegalStateException("The preview model could not be prepared");
        }

        var projection = new Projection();
        projection.setupOrtho(-1000, 1000, WIDTH, HEIGHT, true);
        var device = RenderSystem.getDevice();
        int attachment = GpuTexture.USAGE_RENDER_ATTACHMENT;
        var view = RenderSystem.getModelViewStack();
        var previousProjection = RenderSystem.getProjectionMatrixBuffer();
        var previousProjectionType = RenderSystem.getProjectionType();
        view.pushMatrix();
        try (var matrixBuffer = new ProjectionMatrixBuffer("YSM preview");
             var color = device.createTexture("YSM preview color", attachment | GpuTexture.USAGE_COPY_SRC,
                 GpuFormat.RGBA8_UNORM, WIDTH, HEIGHT, 1, 1);
             var colorView = device.createTextureView(color);
             var depth = device.createTexture("YSM preview depth", attachment, GpuFormat.D32_FLOAT, WIDTH, HEIGHT, 1, 1);
             var depthView = device.createTextureView(depth)) {
            view.identity();
            RenderSystem.setProjectionMatrix(matrixBuffer.getBuffer(projection), ProjectionType.ORTHOGRAPHIC);
            minecraft.gameRenderer.lighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
            var encoder = device.createCommandEncoder();
            try (var frame = minecraft.gameRenderer.featureRenderDispatcher().prepareFrame(nodes);
                 var pass = encoder.createRenderPass(() -> "YSM model preview",
                     colorView, Optional.of(new Vector4f()), depthView, OptionalDouble.of(0))) {
                RenderSystem.bindDefaultUniforms(pass);
                FeatureRenderDispatcher.renderAllFeatures(pass, frame);
            }
            try (var buffer = device.createBuffer(() -> "YSM preview readback",
                GpuBuffer.USAGE_MAP_READ | GpuBuffer.USAGE_COPY_DST, (long) WIDTH * HEIGHT * Integer.BYTES)) {
                encoder.copyTextureToBuffer(color, buffer, 0, () -> {}, 0);
                try (var fence = encoder.createFence()) {
                    encoder.submit();
                    if (!fence.awaitCompletion(5_000_000_000L)) {
                        throw new IllegalStateException("YSM preview readback did not complete");
                    }
                }
                return readPixels(buffer);
            }
        } finally {
            view.popMatrix();
            RenderSystem.setProjectionMatrix(previousProjection, previousProjectionType);
            minecraft.gameRenderer.lighting().setupFor(Lighting.Entry.ITEMS_3D);
        }
    }

    private static NativeImage readPixels(GpuBuffer buffer) {
        try (var mapped = buffer.map(true, false)) {
            var pixels = new NativeImage(WIDTH, HEIGHT, false);
            try {
                var bytes = mapped.data();
                for (int y = 0; y < HEIGHT; y++) {
                    for (int x = 0; x < WIDTH; x++) {
                        pixels.setPixelABGR(x, HEIGHT - y - 1, bytes.getInt((x + y * WIDTH) * Integer.BYTES));
                    }
                }
                return pixels;
            } catch (RuntimeException | Error failure) {
                pixels.close();
                throw failure;
            }
        }
    }

    static boolean disablePreviewRotation(SettingsProto settings) {
        var customPreview = settings.previewAnimation()
            .filter(name -> !name.isBlank() && !name.equals(AnimationRegister.IDLE)).isPresent();
        return settings.disablePreviewRotation() && !customPreview;
    }

    private record BorrowedTargetLease(ResourceRequest request, ModelRenderTarget target) implements ResourceLease {
        @Override
        public ModelResourcePurpose purpose() {
            return request.purpose();
        }

        @Override
        public AcquireResult poll() {
            return new AcquireResult.Ready(target);
        }

        @Override
        public boolean isCurrent(ResourceRequest candidate) {
            return request.equals(candidate);
        }

        @Override
        public void cancelPending() {}

        @Override
        public void close() {}
    }
}
