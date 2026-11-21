package com.elfmcys.ysm.natives.render;

import com.elfmcys.ysm.buffer.NativeStorage;
import com.elfmcys.ysm.buffer.NativeBuffer;
import com.elfmcys.ysm.buffer.annotation.Aligned;
import com.elfmcys.ysm.buffer.annotation.Borrowed;
import com.elfmcys.ysm.client.compat.IrisCompat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4fc;

import java.lang.ref.Reference;

/**
 * Bridges native CPU vertex output to the host's current VertexConsumer.
 *
 * <p>Execution: callers use the host render thread and serialize all calls, including calls
 * for different states. Matrix storage and the native executor are shared.
 * Native worker writes complete before JNI returns; this call can wait for those workers.
 *
 * <p>Ownership: callers retain the state and consumer for the call. Native code borrows output
 * ranges only until JNI returns; the host retains buffer, upload, and GPU ownership.
 *
 * <p>Seam: implements success-before-submit in the
 * [host output contract](docs/architecture/rendering/overview.md#顶点输出与宿主接管).
 */
public class NativeRenderer {
    private static final int SUCCESS = 1;
    private static final int OUTPUT_REJECTED = 2;

    /**
     * Completes CPU output while the caller can still borrow the model state. The host callback
     * writes only the completed vertex data, which already contains the draw's transformed positions.
     */
    public static boolean submit(SubmitNodeCollector collector, RenderType renderType, PoseStack poseStack,
        CameraRenderState camera, NativeModelState modelState, int vertexCount,
        int light, int overlay, int color, RenderContextType contextType) {
        if (!validateVertexCount(modelState, vertexCount)) {
            return false;
        }
        if (vertexCount == 0) {
            return renderEmpty(modelState);
        }

        var vertices = FallbackVertexWriter.allocateVertexData(vertexCount);
        var matrices = getMatBuffer(poseStack.last(), camera.viewRotationMatrix, camera.projectionMatrix);
        var result = 0;
        try {
            result = nRender(vertices, 0, matrices.ptr(), modelState.get(), vertexCount,
                packLightAndOverlay(light, overlay), packColor(color), packFlags(VertexFormatType.FALLBACK, contextType),
                IrisCompat.getEntityId());
        } finally {
            if (result != SUCCESS && result != OUTPUT_REJECTED) {
                modelState.invalidate();
            }
            Reference.reachabilityFence(matrices);
            Reference.reachabilityFence(modelState);
        }
        if (result != SUCCESS) {
            return false;
        }
        collector.submitCustomGeometry(poseStack, renderType,
            (pose, consumer) -> FallbackVertexWriter.write(consumer, vertices, vertexCount, overlay));
        return true;
    }

    private static boolean validateVertexCount(NativeModelState modelState, int vertexCount) {
        if (!modelState.isValid()) {
            return false;
        }
        if (vertexCount != modelState.getTotalVertexCount()) {
            modelState.invalidate();
            return false;
        }
        return true;
    }

    private static boolean renderEmpty(NativeModelState modelState) {
        var result = 0;
        try {
            result = nRender(null, 0, 0, modelState.get(), 0, 0, 0, 0, 0);
            return result == SUCCESS;
        } finally {
            if (result != SUCCESS) {
                modelState.invalidate();
            }
            Reference.reachabilityFence(modelState);
        }
    }

    @Borrowed
    @Aligned(64)
    private static NativeBuffer getMatBuffer(PoseStack.Pose pose, Matrix4fc view, Matrix4fc projection) {
        var buffer = MatBufferHolder.BUFFER;
        var buf = buffer.nio();

        pose.pose().get(buf);
        view.get(64, buf);
        projection.get(128, buf);
        pose.normal().get(192, buf);

        return buffer;
    }

    private static long packFlags(VertexFormatType v, RenderContextType c) {
        return ((long)v.id() << 2) | c.id();
    }

    private static long packLightAndOverlay(int lightUv, int overlayOv) {
        return ((long)lightUv << 32) | overlayOv;
    }

    static int packColor(int argb) {
        return ((argb >>> 16) & 0xFF) | (argb & 0xFF00) | ((argb & 0xFF) << 16) | (argb & 0xFF000000);
    }

    /**
     * JNI returns 1 for completed CPU output, 2 for output-only rejection, and 0 for invalid state
     * or entry failure. A failed call may have written destination bytes; they must not be committed.
     * A positive buffer flag selects a direct carrier; otherwise the carrier is int[].
     * Empty output validates publication without reading the buffer or matrices.
     */
    private static native int nRender(Object vertexBuffer, int vertexBufferFlag, long matPtr, long modelStatePtr,
        int vertexCount, long lightAndOverlay, int color, long flags, long irisEntityId);

    private static final class MatBufferHolder {
        private static final NativeStorage BUFFER = NativeBuffer.allocate(256, 64);
    }

}
