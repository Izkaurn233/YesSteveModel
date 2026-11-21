package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.client.entity.FirstPersonPart;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;

public final class RenderFirstPlayerBackground {
    private RenderFirstPlayerBackground() {}

    public static void submit(PoseStack pose, SubmitNodeCollector collector, int light, float partialTick) {
        pose.pushPose();
        try {
            // The hand pass already applies view bobbing to its base pose.
            pose.translate(0, -1.5, 0);
            ReplacePlayerHandRenderEvent.submit(FirstPersonPart.BACKGROUND, pose, collector, light,
                OverlayTexture.NO_OVERLAY, partialTick);
        } finally {
            pose.popPose();
        }
    }
}
