package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.network.NetworkHandler;
import com.elfmcys.ysm.network.forge.ClientProtocolGateway;
import net.minecraft.client.player.LocalPlayer;

// PORT(26.3-fabric): called from ClientPacketListenerRespawnMixin (Forge ClientPlayerNetworkEvent.Clone).
public class LocalPlayerRespawnEvent {
    public static void onFire(LocalPlayer oldPlayer, LocalPlayer newPlayer) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        // 仅客户端运行时，执行后续的 copyFrom 会出现 null 值问题
        if (!NetworkHandler.isRemoteChannelPresent()) {
            return;
        }

        // Forge needed reviveCaps()/invalidateCaps() around this; data attachments stay readable on the old player.
        oldPlayer.getCapability(PlayerAnimatableCapabilityProvider.CAP)
            .ifPresent(oldCap -> newPlayer.getCapability(PlayerAnimatableCapabilityProvider.CAP)
                .ifPresent(newCap -> newCap.moveFrom(oldCap)));
        ClientProtocolGateway.localPlayerCloned();
    }
}
