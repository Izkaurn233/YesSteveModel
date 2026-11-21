package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.client.entity.FirstPersonPart;
import com.elfmcys.ysm.config.ClientConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.jetbrains.annotations.Nullable;

public final class ReplacePlayerHandRenderEvent {
    private static @Nullable CameraRenderState camera;
    private static float partialTick;

    private ReplacePlayerHandRenderEvent() {}

    public static void inHandPass(CameraRenderState drawCamera, float sample, Runnable submit) {
        var previousCamera = camera;
        var previousSample = partialTick;
        camera = drawCamera;
        partialTick = sample;
        try {
            submit.run();
        } finally {
            camera = previousCamera;
            partialTick = previousSample;
        }
    }

    public static boolean submit(FirstPersonPart part, PoseStack pose, SubmitNodeCollector collector, int light,
        int overlay) {
        return submit(part, pose, collector, light, overlay, partialTick);
    }

    static boolean submit(FirstPersonPart part, PoseStack pose, SubmitNodeCollector collector, int light,
        int overlay, float sample) {
        if (camera == null || !YesSteveModel.isAvailable() || ClientConfig.DISABLE_SELF_MODEL.get()
            || ClientConfig.DISABLE_SELF_HANDS.get()) {
            return false;
        }
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }
        var cap = player.getCapability(PlayerAnimatableCapabilityProvider.CAP).orElse(null);
        return cap != null && RegisterEntityRenderersEvent.getFirstPersonArmRenderer()
            .render(player, cap, part, pose, collector, camera, light, overlay, sample);
    }
}
