package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.accessor.VertexBufferAccessor;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * PORT(26.3-fabric): bulk entity-vertex append for the 26.3 BufferBuilder. Members verified against the decompiled
 * 26.3 class: {@code beginVertex()} = ensureBuilding + endLastVertex + vertices++ + {@code buffer.reserve(vertexSize)}
 * into {@code vertexPointer}; {@code endLastVertex()} only checks {@code elementsToFill == 0} (plus the LINES
 * duplicate). Appending N complete vertices therefore reserves N * vertexSize at once, copies, bumps
 * {@code vertices} by N, leaves {@code vertexPointer} on the last vertex and clears {@code elementsToFill}.
 */
@Mixin(BufferBuilder.class)
public abstract class BufferBuilderMixin implements VertexBufferAccessor {
    @Unique
    private static final int YSM_MAX_VERTEX_COUNT = 0xFFFFFF;

    @Shadow
    @Final
    private ByteBufferBuilder buffer;
    @Shadow
    private long vertexPointer;
    @Shadow
    private int vertices;
    @Shadow
    @Final
    private PrimitiveTopology primitiveTopology;
    @Shadow
    @Final
    private boolean entityFormat;
    @Shadow
    @Final
    private int vertexSize;
    @Shadow
    private int elementsToFill;
    @Shadow
    private boolean building;

    @Shadow
    protected abstract void ensureBuilding();

    @Shadow
    protected abstract void endLastVertex();

    @Unique
    @Override
    public boolean ysm$acceptsEntityVertices() {
        return building && entityFormat && primitiveTopology != PrimitiveTopology.LINES;
    }

    @Unique
    @Override
    public void ysm$appendEntityVertices(long source, int vertexCount) {
        if (vertexCount <= 0) {
            return;
        }
        ensureBuilding();
        endLastVertex();
        if (vertexCount > YSM_MAX_VERTEX_COUNT - this.vertices) {
            throw new IllegalStateException("Trying to write too many vertices (>16777215) into BufferBuilder");
        }
        int bytes = Math.multiplyExact(vertexCount, this.vertexSize);
        long destination = this.buffer.reserve(bytes);
        MemoryUtil.memCopy(source, destination, bytes);
        this.vertices += vertexCount;
        this.vertexPointer = destination + (long)(vertexCount - 1) * this.vertexSize;
        this.elementsToFill = 0;
    }
}
