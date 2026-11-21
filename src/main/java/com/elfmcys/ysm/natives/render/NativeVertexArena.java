package com.elfmcys.ysm.natives.render;

import com.elfmcys.ysm.buffer.BorrowedNativeBuffer;
import com.elfmcys.ysm.buffer.NativeBuffer;
import com.elfmcys.ysm.buffer.NativeStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * Frame-scoped native memory for completed model vertices, waiting for the host to hand over its consumer.
 *
 * <p>PORT(26.3-fabric): 26.3 delivers the destination {@code VertexConsumer} in a
 * {@code submitCustomGeometry} callback, which runs in the same frame's {@code FeatureRenderDispatcher.prepareFrame},
 * after the model-state borrow has ended. Native output is written here at submit time and copied in one block
 * when the callback runs.
 *
 * <p>Lifetime: {@link #beginFrame()} runs at the head of {@code Minecraft.renderFrame} and makes every earlier
 * allocation reusable. Chunks are kept and reused, never freed while the game runs, so a late reader can only see
 * stale bytes, never freed memory. {@link Allocation#isCurrent()} lets a reader detect that and skip.
 *
 * <p>Execution: render thread only; no synchronisation.
 */
final class NativeVertexArena {
    private static final int CHUNK_BYTES = 1 << 20;
    private static final int ALIGNMENT = 64;

    private static final List<NativeStorage> CHUNKS = new ArrayList<>();
    private static int chunkIndex;
    private static int chunkOffset;
    private static int generation;

    private NativeVertexArena() {
    }

    static void beginFrame() {
        generation++;
        chunkIndex = 0;
        chunkOffset = 0;
    }

    static Allocation allocate(int bytes) {
        if (bytes <= 0) {
            throw new IllegalArgumentException("Allocation must be positive: " + bytes);
        }
        while (true) {
            if (chunkIndex == CHUNKS.size()) {
                CHUNKS.add(NativeBuffer.allocate(Math.max(CHUNK_BYTES, align(bytes)), ALIGNMENT));
            }
            NativeStorage chunk = CHUNKS.get(chunkIndex);
            int start = align(chunkOffset);
            if (start <= chunk.size() - bytes) {
                chunkOffset = start + bytes;
                return new Allocation(chunk.slice(start, bytes), generation);
            }
            if (chunkOffset == 0) {
                // An oversized request found an older, smaller chunk at this index; replace it.
                CHUNKS.set(chunkIndex, NativeBuffer.allocate(align(bytes), ALIGNMENT)).close();
                continue;
            }
            chunkIndex++;
            chunkOffset = 0;
        }
    }

    private static int align(int value) {
        return Math.addExact(value, ALIGNMENT - 1) & -ALIGNMENT;
    }

    record Allocation(BorrowedNativeBuffer region, int generation) {
        boolean isCurrent() {
            return generation == NativeVertexArena.generation;
        }
    }
}
