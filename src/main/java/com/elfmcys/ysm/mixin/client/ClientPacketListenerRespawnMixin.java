package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.client.event.LocalPlayerRespawnEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * PORT(26.3-fabric): replaces Forge {@code ClientPlayerNetworkEvent.Clone}, which Fabric API has no counterpart for.
 * Target verified against the 26.3 jar: {@code handleRespawn(Lnet/minecraft/network/protocol/game/ClientboundRespawnPacket;)V}.
 * The first, off-thread call reschedules itself by throwing, so RETURN only runs on the main-thread pass and HEAD is
 * re-captured there.
 */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerRespawnMixin {
    @Unique
    private LocalPlayer ysm$playerBeforeRespawn;

    @Inject(method = "handleRespawn(Lnet/minecraft/network/protocol/game/ClientboundRespawnPacket;)V", at = @At("HEAD"))
    private void ysm$captureOldPlayer(ClientboundRespawnPacket packet, CallbackInfo ci) {
        this.ysm$playerBeforeRespawn = Minecraft.getInstance().player;
    }

    @Inject(method = "handleRespawn(Lnet/minecraft/network/protocol/game/ClientboundRespawnPacket;)V", at = @At("RETURN"))
    private void ysm$firePlayerClone(ClientboundRespawnPacket packet, CallbackInfo ci) {
        LocalPlayer oldPlayer = this.ysm$playerBeforeRespawn;
        LocalPlayer newPlayer = Minecraft.getInstance().player;
        this.ysm$playerBeforeRespawn = null;
        if (oldPlayer != null && newPlayer != null && oldPlayer != newPlayer) {
            LocalPlayerRespawnEvent.onFire(oldPlayer, newPlayer);
        }
    }
}
