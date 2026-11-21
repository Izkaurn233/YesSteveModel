package com.elfmcys.ysm.network.forge;

import com.elfmcys.ysm.platform.network.Connections;
import com.elfmcys.ysm.network.NetworkHandler;
import com.elfmcys.ysm.network.dispatch.SendResult;
import com.elfmcys.ysm.network.dispatch.TransportPort;
import com.elfmcys.ysm.network.frame.OutboundFrame;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

/** Netty-specific pressure and submission details kept behind TransportPort. */
public final class ForgeTransportPort implements TransportPort {
    private final ServerPlayer player;

    public ForgeTransportPort(ServerPlayer player) {
        this.player = Objects.requireNonNull(player, "player");
    }

    @Override
    public boolean isOpen() {
        return player.connection != null && Connections.of(player).isConnected();
    }

    @Override
    public boolean isWritable() {
        return isOpen() && Connections.channel(Connections.of(player)).isWritable();
    }

    @Override
    public long highWatermarkBytes() {
        return Connections.channel(Connections.of(player)).config().getWriteBufferHighWaterMark();
    }

    @Override
    public long pendingBytes() {
        var outbound = Connections.channel(Connections.of(player)).unsafe().outboundBuffer();
        return outbound == null ? 0 : outbound.totalPendingWriteBytes();
    }

    @Override
    public SendResult trySend(OutboundFrame frame) {
        if (!isOpen()) {
            return SendResult.FAILED;
        }
        try {
            NetworkHandler.sendFrame(player, frame);
            return SendResult.SUCCESS;
        } catch (RuntimeException failure) {
            return SendResult.FAILED;
        }
    }
}
