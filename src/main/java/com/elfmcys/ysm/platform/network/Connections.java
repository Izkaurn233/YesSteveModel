package com.elfmcys.ysm.platform.network;

import com.elfmcys.ysm.mixin.accessor.ConnectionAccessor;
import com.elfmcys.ysm.mixin.accessor.ServerCommonPacketListenerAccessor;
import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/** Access to connection internals that 26.3 no longer exposes (see the accessor mixins). */
public final class Connections {
    private Connections() {
    }

    /** Replaces {@code connection.channel()}; may be null before the channel is active, as before. */
    public static @Nullable Channel channel(Connection connection) {
        return ((ConnectionAccessor)connection).ysm$getChannel();
    }

    /** Replaces {@code player.connection.connection}. */
    public static Connection of(ServerPlayer player) {
        return ((ServerCommonPacketListenerAccessor)player.connection).ysm$getConnection();
    }
}
