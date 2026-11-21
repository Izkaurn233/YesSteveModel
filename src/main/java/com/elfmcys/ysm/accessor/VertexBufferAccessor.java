package com.elfmcys.ysm.accessor;

/**
 * Bulk append of complete vertices in the host builder's entity layout.
 *
 * <p>PORT(26.3-fabric): 26.3 rebuilt {@code BufferBuilder} around {@code ByteBufferBuilder.reserve} and raw
 * pointers, so the 1.20.1 reserve/advance borrow (native writing into the builder's ByteBuffer) no longer applies.
 * Native output is now produced into YSM-owned memory first, while the model state is still borrowed, and copied
 * in one block when the host hands over the consumer (see {@code NativeRenderer}).
 *
 * <p>Execution: render thread only. No other write to the same builder may interleave with an append.
 *
 * <p>Seam: provides the host side of the
 * [frame borrow contract](docs/architecture/native-runtime/implementation/jni-and-memory.md#帧内借用).
 */
public interface VertexBufferAccessor {
    /**
     * Whether {@link #ysm$appendEntityVertices} may be used: the builder is building, uses
     * {@code DefaultVertexFormat.ENTITY} (36-byte vertices) and is not a LINES builder (LINES duplicates vertices).
     */
    boolean ysm$acceptsEntityVertices();

    /**
     * Appends {@code vertexCount} complete vertices that start at native address {@code source}, in the 26.3
     * {@code DefaultVertexFormat.ENTITY} layout: position 3xf32, color RGBA8, UV0 2xf32, UV1 (overlay) 2xs16,
     * UV2 (light) 2xs16, normal RGBA8_SNORM.
     *
     * <p>Requires: {@link #ysm$acceptsEntityVertices()} holds and the source range is readable for
     * {@code vertexCount * 36} bytes. Ensures: the builder state equals having added those vertices one by one.
     */
    void ysm$appendEntityVertices(long source, int vertexCount);
}
