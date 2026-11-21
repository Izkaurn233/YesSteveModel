package com.elfmcys.ysm.natives.render;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.accessor.VertexBufferAccessor;
import com.elfmcys.ysm.natives.NativeProfiler;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.lwjgl.system.MemoryUtil;

import java.lang.ref.Reference;

/**
 * Hands completed VANILLA-layout native output to the host consumer.
 *
 * <p>Layout (36 bytes, identical to 26.3 {@code DefaultVertexFormat.ENTITY} as decompiled from
 * {@code BufferBuilder.addVertex}): +0 position 3xf32, +12 color R,G,B,A bytes, +16 UV0 2xf32, +24 overlay
 * (packed u | v << 16), +28 light (packed), +32 normal x,y,z signed bytes (+35 unused).
 *
 * <p>Fast path: an entity-format {@code BufferBuilder} receives one {@code memCopy}. Any other consumer gets the
 * vertices decoded and replayed one by one, which is the slow path the profile showed.
 */
final class VanillaVertexReader {
    static final int STRIDE = 36;

    private static boolean warnedStale;

    private VanillaVertexReader() {
    }

    static void write(VertexConsumer consumer, NativeVertexArena.Allocation allocation, int vertexCount) {
        if (!allocation.isCurrent()) {
            // The callback ran after the arena was reset for a later frame: the bytes may be reused. Skip this draw.
            if (!warnedStale) {
                warnedStale = true;
                YesSteveModel.LOGGER.warn("Dropping YSM model geometry consumed after its frame ended");
            }
            return;
        }
        var region = allocation.region();
        try {
            long base = region.ptr();
            if (consumer instanceof VertexBufferAccessor accessor && accessor.ysm$acceptsEntityVertices()) {
                accessor.ysm$appendEntityVertices(base, vertexCount);
                return;
            }
            replay(consumer, base, vertexCount);
        } finally {
            Reference.reachabilityFence(region);
        }
    }

    private static void replay(VertexConsumer consumer, long base, int vertexCount) {
        try (var ignored = NativeProfiler.beginFallbackVertexWrite()) {
            for (int i = 0; i < vertexCount; i++) {
                long p = base + (long)i * STRIDE;
                int r = MemoryUtil.memGetByte(p + 12) & 0xFF;
                int g = MemoryUtil.memGetByte(p + 13) & 0xFF;
                int b = MemoryUtil.memGetByte(p + 14) & 0xFF;
                int a = MemoryUtil.memGetByte(p + 15) & 0xFF;
                consumer.addVertex(MemoryUtil.memGetFloat(p), MemoryUtil.memGetFloat(p + 4),
                    MemoryUtil.memGetFloat(p + 8), (a << 24) | (r << 16) | (g << 8) | b,
                    MemoryUtil.memGetFloat(p + 16), MemoryUtil.memGetFloat(p + 20),
                    packedUv(p + 24), packedUv(p + 28),
                    MemoryUtil.memGetByte(p + 32) / 127f, MemoryUtil.memGetByte(p + 33) / 127f,
                    MemoryUtil.memGetByte(p + 34) / 127f);
            }
        }
    }

    /** Inverse of {@code BufferBuilder.putPackedUv}: two little-endian shorts, u in the low half. */
    private static int packedUv(long pointer) {
        int u = MemoryUtil.memGetShort(pointer) & 0xFFFF;
        int v = MemoryUtil.memGetShort(pointer + 2) & 0xFFFF;
        return u | (v << 16);
    }
}
