package com.elfmcys.ysm.client.texture;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.natives.image.ImageSource;
import com.elfmcys.ysm.model.storage.PreviewStore;
import com.elfmcys.ysm.model.storage.PreviewPixels;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class CustomTexture extends YsmTexture {
    private final ImageSource source;
    private final Decoder decoder;
    private final Uploader uploader;
    private @Nullable Throwable failure;
    private PreviewPixels prepared;
    private final PreviewStore.EncodedPreview ownedSource;
    private final java.util.function.BiConsumer<CustomTexture, PreviewPixels> preparedUploader;
    private boolean closed;
    private Runnable invalidated = () -> {};
    private boolean loaded;

    public CustomTexture(ImageSource source) {
        this(source, CustomTexture::decode, CustomTexture::upload);
    }

    CustomTexture(ImageSource source, Decoder decoder, Uploader uploader) {
        this(source, decoder, uploader, CustomTexture::uploadPrepared);
    }

    CustomTexture(ImageSource source, Decoder decoder, Uploader uploader,
        java.util.function.BiConsumer<CustomTexture, PreviewPixels> preparedUploader) {
        Objects.requireNonNull(source, "source");
        if (source instanceof PreviewStore.EncodedPreview encoded) {
            ownedSource = encoded.acquireContent();
            prepared = encoded.takePixels();
            this.source = ownedSource;
        } else {
            ownedSource = null;
            this.source = source;
        }
        this.preparedUploader = Objects.requireNonNull(preparedUploader, "preparedUploader");
        this.decoder = Objects.requireNonNull(decoder, "decoder");
        this.uploader = Objects.requireNonNull(uploader, "uploader");
    }

    @Override
    public void load(ResourceManager resourceManager) {
        RenderSystem.assertOnRenderThread();
        if (closed || failure != null) {
            return;
        }
        if (loaded)
            invalidated.run();
        loaded = true;
        var initial = prepared;
        prepared = null;
        try (var reload = initial == null && ownedSource != null ? ownedSource.beginReload() : null; initial) {
            if (initial != null) {
                preparedUploader.accept(this, initial);
            } else {
                try (var pixels = decoder.decode(source)) {
                    uploader.upload(this, pixels);
                }
            }
        } catch (Exception error) {
            failure = error;
            YesSteveModel.LOGGER.debug("Failed to load standalone GUI texture from {}", source, error);
        }
    }

    private static NativeImage decode(ImageSource source) throws Exception {
        try (var image = source.open()) {
            return image.decode();
        }
    }

    private static void upload(CustomTexture texture, NativeImage img) {
        texture.uploadPixels(img);
    }

    private static void uploadPrepared(CustomTexture texture, PreviewPixels pixels) {
        texture.uploadPixels(pixels);
    }

    @Override
    public void close() {
        if (closed)
            return;
        closed = true;
        invalidated.run();
        var closing = prepared;
        prepared = null;
        try {
            if (closing != null)
                closing.close();
        } finally {
            try {
                if (ownedSource != null)
                    ownedSource.close();
            } finally {
                super.close();
            }
        }
    }

    void onInvalidated(Runnable listener) {
        invalidated = Objects.requireNonNull(listener);
    }

    public Optional<Throwable> failure() {
        return Optional.ofNullable(failure);
    }

    @FunctionalInterface
    interface Decoder {
        NativeImage decode(ImageSource source) throws Exception;
    }

    @FunctionalInterface
    interface Uploader {
        void upload(CustomTexture texture, NativeImage image);
    }
}
