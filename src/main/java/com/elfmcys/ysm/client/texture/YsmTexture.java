package com.elfmcys.ysm.client.texture;

import com.elfmcys.ysm.model.storage.PreviewPixels;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import java.io.IOException;
import java.util.function.BiConsumer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;

/** A model or GUI texture whose source and CPU pixels are owned by YSM. */
public abstract class YsmTexture extends AbstractTexture {
    public abstract void load(ResourceManager resourceManager) throws IOException;

    final void uploadPixels(NativeImage image) {
        replacePixels(image.getWidth(), image.getHeight(), (encoder, destination) ->
            encoder.writeToTexture(destination, image));
    }

    final void uploadPixels(PreviewPixels pixels) {
        replacePixels(pixels.width(), pixels.height(), (encoder, destination) ->
            pixels.borrow(data -> encoder.writeToTexture(destination, data, 0, 0, 0, 0,
                pixels.width(), pixels.height())));
    }

    private void replacePixels(int width, int height, BiConsumer<CommandEncoder, GpuTexture> upload) {
        RenderSystem.assertOnRenderThread();
        var device = RenderSystem.getDevice();
        var nextSampler = RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST);
        var nextTexture = device.createTexture(getClass()::getSimpleName,
            GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING, GpuFormat.RGBA8_UNORM,
            width, height, 1, 1);
        GpuTextureView nextView = null;
        try {
            nextView = device.createTextureView(nextTexture);
            upload.accept(device.createCommandEncoder(), nextTexture);
            releaseTextures();
        } catch (RuntimeException | Error failure) {
            if (nextView != null) {
                try {
                    nextView.close();
                } catch (RuntimeException | Error closeFailure) {
                    failure.addSuppressed(closeFailure);
                }
            }
            try {
                nextTexture.close();
            } catch (RuntimeException | Error closeFailure) {
                failure.addSuppressed(closeFailure);
            }
            throw failure;
        }
        texture = nextTexture;
        textureView = nextView;
        sampler = nextSampler;
    }
}
