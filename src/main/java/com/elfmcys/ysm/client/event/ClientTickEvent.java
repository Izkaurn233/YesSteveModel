package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.model.service.ClientModelService;
import com.elfmcys.ysm.client.texture.CustomTextureManager;
import com.elfmcys.ysm.config.ClientConfig;
import com.elfmcys.ysm.network.forge.ClientProtocolGateway;
import net.minecraft.client.Minecraft;

// PORT(26.3-fabric): registered on Fabric ClientTickEvents.START_CLIENT_TICK (Forge TickEvent.Phase.START).
public class ClientTickEvent {
    private static int tickCount;
    private static int refreshRate = 60;

    public static void onClientTick() {
        if (!YesSteveModel.isAvailable()) {
            RegisterEntityRenderersEvent.releaseFirstPersonArm();
            return;
        }
        tickCount++;
        CustomTextureManager.tick();
        ClientModelService.current().ifPresent(ClientModelService::tick);
        // PORT(26.3-fabric): Window.getRefreshRate() is gone; the active SDL video mode carries a float refresh rate.
        var videoMode = Minecraft.getInstance().getWindow().getActiveVideoMode();
        if (videoMode != null && videoMode.getRefreshRate() > 0) {
            refreshRate = Math.round(videoMode.getRefreshRate());
        }

        var player = Minecraft.getInstance().player;
        if (player == null || ClientConfig.DISABLE_SELF_MODEL.get() || ClientConfig.DISABLE_SELF_HANDS.get()) {
            RegisterEntityRenderersEvent.releaseFirstPersonArm();
        }
        if (player != null) {
            var capability = player.getCapability(PlayerAnimatableCapabilityProvider.CAP).orElse(null);
            if (capability == null || !capability.isPresentationAllowed()) {
                RegisterEntityRenderersEvent.releaseFirstPersonArm();
            }
            if (capability != null) {
                ClientProtocolGateway.tick(player, capability);
            }
        }
    }

    public static int getTickCount() {
        return tickCount;
    }

    public static int getRefreshRate() {
        return refreshRate;
    }
}
