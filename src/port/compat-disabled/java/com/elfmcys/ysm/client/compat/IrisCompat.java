package com.elfmcys.ysm.client.compat;

import com.elfmcys.ysm.client.texture.CustomPBRTextureSet;
import com.elfmcys.ysm.natives.render.VertexFormatType;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.minecraft.client.renderer.texture.AbstractTexture;

import java.util.Optional;
import java.util.function.Consumer;

/** Neutral result for the excluded Forge integration. */
public final class IrisCompat {
    private IrisCompat() {}

    public static void init() {}

    public static Optional<VertexFormatType> determineVertexFormatType(VertexFormat format) {
        return Optional.empty();
    }

    public static boolean isInstalled() {
        return false;
    }

    public static boolean isRenderingShadow() {
        return false;
    }

    public static long getEntityId() {
        return 0;
    }

    public static void requirePbrTextures(CustomPBRTextureSet texture, Consumer<AbstractTexture> hostOwnership) {
        throw new IllegalStateException("Oculus PBR integration is not installed");
    }
}
