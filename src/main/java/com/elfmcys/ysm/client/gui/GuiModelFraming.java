package com.elfmcys.ysm.client.gui;

import com.elfmcys.ysm.geckolib3.geo.animated.GeoModelState;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4d;
import org.joml.Matrix4f;

/** Fits texture-card geometry in the picture-in-picture canvas while the pose is borrowed. */
public final class GuiModelFraming {
    private final GuiModelView view = new GuiModelView();
    private final GuiModelView.Bounds bounds = new GuiModelView.Bounds();
    private final Matrix4f bonePose = new Matrix4f();
    private final Matrix4d transform = new Matrix4d();
    private final Matrix4d boneTransform = new Matrix4d();
    private Object installation;
    public boolean applyPreview(Object installation, GeoModelState state, PoseStack poseStack, float width, float height) {
        return fit(installation, state, poseStack, width, height, width / 2, height / 2, 0);
    }

    private boolean fit(Object installation, GeoModelState state, PoseStack poseStack, float width, float height,
        float targetX, float targetY, float targetZ) {
        if (this.installation != installation) {
            this.installation = installation;
            view.reset();
        }
        if (!state.isValid() || state.getVertexCount() == 0)
            return false;
        var geometry = state.getModel().guiBoneBounds();
        var nativeState = state.getNativeState();
        var bones = nativeState.getRenderBoneIndices();
        var poses = nativeState.getBonePoses();
        bounds.clear();
        for (var i = 0; i < bones.capacity(); i++) {
            var bone = Short.toUnsignedInt(bones.get(i));
            var offset = bone * 6;
            if (geometry[offset] > geometry[offset + 3])
                continue;
            poses.getPose(bone, bonePose);
            transform.set(poseStack.last().pose()).mul(boneTransform.set(bonePose));
            for (var corner = 0; corner < 8; corner++) {
                double x = geometry[offset + ((corner & 1) == 0 ? 0 : 3)];
                double y = geometry[offset + ((corner & 2) == 0 ? 1 : 4)];
                double z = geometry[offset + ((corner & 4) == 0 ? 2 : 5)];
                bounds.include(transform.m00() * x + transform.m10() * y + transform.m20() * z + transform.m30(),
                    transform.m01() * x + transform.m11() * y + transform.m21() * z + transform.m31(),
                    transform.m02() * x + transform.m12() * y + transform.m22() * z + transform.m32());
            }
        }
        var camera = view.fit(bounds, width, height);
        if (camera == null)
            return false;
        var scale = camera.scale();
        transform.translation(targetX - scale * camera.centerX(), targetY - scale * camera.centerY(), targetZ - scale * camera.centerZ())
            .scale(scale).mul(boneTransform.set(poseStack.last().pose()));
        poseStack.last().pose().set(transform);
        // A positive uniform camera scale preserves the existing normal directions.
        return poseStack.last().pose().isFinite();
    }

}
