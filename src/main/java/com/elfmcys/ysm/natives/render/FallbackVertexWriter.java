package com.elfmcys.ysm.natives.render;

import com.elfmcys.ysm.natives.NativeProfiler;
import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Replays completed native output to an unknown host consumer layout.
 *
 * The carrier belongs to a single submitted draw.
 *
 * <p>Seam: implements the fallback branch of the
 * [host output handoff](docs/architecture/rendering/implementation/vertex-output.md#rendertypecontext-与-vertexkind).
 */
class FallbackVertexWriter {
    private static final int STRIDE = 8;

    private static final int INDEX_COLOR = 0;
    private static final int INDEX_NORMAL = 1;
    private static final int INDEX_X = 2;
    private static final int INDEX_Y = 3;
    private static final int INDEX_Z = 4;
    private static final int INDEX_TEX_U = 5;
    private static final int INDEX_TEX_V = 6;
    private static final int INDEX_LIGHT = 7;

    static int[] allocateVertexData(int vertexCount) {
        return new int[Math.multiplyExact(vertexCount, STRIDE)];
    }

    /**
     * <p>Requires: native rendering succeeded for exactly this prefix of the current carrier.
     *
     * <p>Failure: consumer exceptions propagate unchanged after any already accepted prefix.
     * The caller must not replay that prefix as a recovery attempt.
     */
    static void write(VertexConsumer vertexBuffer, int[] vertexData, int vertexCount, int overlayUv) {
        try (var ignored = NativeProfiler.beginFallbackVertexWrite()) {
            for (int i = 0; i < vertexCount; i++) {
                var offset = i * STRIDE;

                var color = vertexData[offset + INDEX_COLOR];
                var argb = (color & 0xFF00FF00) | ((color & 0xFF) << 16) | ((color >>> 16) & 0xFF);

                var normal = vertexData[offset + INDEX_NORMAL];
                var normalX = ((byte)((normal & 0xFF))) / 127f;
                var normalY = ((byte)((normal >>> 8) & 0xFF)) / 127f;
                var normalZ = ((byte)((normal >>> 16) & 0xFF)) / 127f;

                vertexBuffer.addVertex(Float.intBitsToFloat(vertexData[offset + INDEX_X]),
                    Float.intBitsToFloat(vertexData[offset + INDEX_Y]),
                    Float.intBitsToFloat(vertexData[offset + INDEX_Z]), argb,
                    Float.intBitsToFloat(vertexData[offset + INDEX_TEX_U]),
                    Float.intBitsToFloat(vertexData[offset + INDEX_TEX_V]), overlayUv, vertexData[offset + INDEX_LIGHT],
                    normalX, normalY, normalZ);
            }
        }
    }
}
