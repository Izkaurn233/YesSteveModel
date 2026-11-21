package com.elfmcys.ysm.network;

import com.elfmcys.ysm.platform.network.Connections;
import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.buffer.ArrayBuffer;
import com.elfmcys.ysm.buffer.UniBuffer;
import com.elfmcys.ysm.network.forge.ClientSessionRuntime;
import com.elfmcys.ysm.network.forge.ForgeUniBufferIO;
import com.elfmcys.ysm.network.frame.FrameCodec;
import com.elfmcys.ysm.network.frame.OutboundFrame;
import com.elfmcys.ysm.network.protocol.MessageDirection;
import com.elfmcys.ysm.network.protocol.ProtocolMessageSpec;
import com.elfmcys.ysm.network.protocol.ProtocolMessages;
import com.elfmcys.ysm.network.protocol.ProtocolVersion;
import com.elfmcys.ysm.platform.network.PacketContext;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import us.hebi.quickbuf.ProtoMessage;

import java.util.function.Consumer;

/**
 * The sole host adapter for the current YSM frame protocol.
 * <p>
 * PORT(26.3-fabric): one Fabric custom payload ({@link YsmFramePayload}) replaces the Forge
 * {@code EventNetworkChannel}. Frame encoding, validation and dispatch are unchanged. Differences from Forge:
 * <ul>
 * <li>Fabric invokes payload receivers on the game thread (Forge: netty thread), so frame decoding and
 * {@code ProtocolInbound.dispatch} now run there. See PROJECT_MEMORY.md, network notes.</li>
 * <li>Channel presence is Fabric's {@code canSend}, known once channel registration has been exchanged.</li>
 * </ul>
 */
public final class NetworkHandler {
    public static final String VERSION = ProtocolVersion.TRANSPORT_VERSION;
    public static final Identifier CHANNEL_NAME =
        Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, ProtocolVersion.CHANNEL_PATH);

    private static volatile @Nullable MinecraftServer server;

    private NetworkHandler() {}

    /** Disabled by default; enables temporary wire-cost observations without changing packet flow. */
    public static void traceNetwork(String event, Object... arguments) {
        if (Boolean.getBoolean("ysm.trace_network_cost")) {
            YesSteveModel.LOGGER.info("YSM_NETWORK_TRACE event={} {}", event, java.util.Arrays.toString(arguments));
        }
    }

    /** Both sides: payload types, the server receiver and the server reference used for broadcasts. */
    public static void init() {
        PayloadTypeRegistry.serverboundPlay().register(YsmFramePayload.TYPE, YsmFramePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(YsmFramePayload.TYPE, YsmFramePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(YsmFramePayload.TYPE, (payload, context) -> {
            var player = context.player();
            receive(payload, MessageDirection.CLIENT_TO_SERVER,
                new PacketContext(Connections.of(player), player, context.server()));
        });
        ServerLifecycleEvents.SERVER_STARTING.register(started -> server = started);
        ServerLifecycleEvents.SERVER_STOPPED.register(stopped -> {
            if (server == stopped) {
                server = null;
            }
        });
    }

    /** Client only: the clientbound receiver. */
    @Environment(EnvType.CLIENT)
    public static void initClient() {
        ClientPlayNetworking.registerGlobalReceiver(YsmFramePayload.TYPE, (payload, context) -> {
            var listener = context.client().getConnection();
            if (listener == null) {
                return;
            }
            receive(payload, MessageDirection.SERVER_TO_CLIENT,
                new PacketContext(listener.getConnection(), null, context.client()));
        });
    }

    @SuppressWarnings("ConstantValue")
    public static boolean isPlayerChannelPresent(ServerPlayer player) {
        return player.connection != null && ServerPlayNetworking.canSend(player, YsmFramePayload.TYPE);
    }

    public static boolean isRemoteChannelPresent() {
        ClientPacketListener listener = Minecraft.getInstance().getConnection();
        return listener != null && isChannelPresent(listener.getConnection());
    }

    /**
     * Whether the peer of {@code connection} accepts YSM frames. Callers pass the current client connection.
     * TODO(26.3-fabric): not verified: that Fabric has exchanged play-channel registration before
     * ClientSessionRuntime first asks at session start (Forge knew it from the login handshake).
     */
    public static boolean isChannelPresent(@Nullable Connection connection) {
        if (connection == null || Connections.channel(connection) == null) {
            return false;
        }
        ClientPacketListener listener = Minecraft.getInstance().getConnection();
        return listener != null && listener.getConnection() == connection
            && ClientPlayNetworking.canSend(YsmFramePayload.TYPE);
    }

    public static int sendToServer(Connection connection, NetworkPayload<?> payload) {
        try (payload; var protobuf = ProtocolBuffer.serialize(payload.protobuf());
            var empty = ArrayBuffer.allocate(0)) {
            var spec = messageSpec(payload.protobuf(), MessageDirection.CLIENT_TO_SERVER);
            var raw = payload.raw().<UniBuffer>map(value -> value).orElse(empty);
            spec.attachmentPolicy().validate(raw.size());
            try (var frame = FrameCodec.encode(spec.id(), protobuf, raw)) {
                traceNetwork("outbound", "direction=" + MessageDirection.CLIENT_TO_SERVER,
                    "message=" + payload.protobuf().getClass().getSimpleName(), "protocolId=" + spec.id(),
                    "protobufBytes=" + protobuf.size(), "attachmentBytes=" + raw.size(), "frameBytes=" + frame.size());
                connection.send(serverbound(frame));
                return frame.size();
            }
        }
    }

    public static void sendToServer(Connection connection, ProtoMessage<?> message) {
        sendToServer(connection, payload(message));
    }

    public static void sendToServer(ProtoMessage<?> message) {
        sendToServer(payload(message));
    }

    public static void sendToServer(NetworkPayload<?> payload) {
        if (!isRemoteChannelPresent()) {
            payload.close();
            return;
        }
        var listener = Minecraft.getInstance().getConnection();
        if (listener == null) {
            payload.close();
            return;
        }
        sendToServer(listener.getConnection(), payload);
    }

    public static void sendToClientPlayer(ProtoMessage<?> message, Player player) {
        sendToClientPlayer(payload(message), player);
    }

    public static void sendToClientPlayer(NetworkPayload<?> payload, Player player) {
        var target = (ServerPlayer)player;
        sendToClients(payload, packet -> target.connection.send(packet));
    }

    public static void broadcastToAllPlayers(ProtoMessage<?> message) {
        var current = server;
        var payload = payload(message);
        if (current == null) {
            payload.close();
            return;
        }
        sendToClients(payload, packet -> {
            for (ServerPlayer player : PlayerLookup.all(current)) {
                player.connection.send(packet);
            }
        });
    }

    /** Forge {@code TRACKING_ENTITY}: players tracking {@code centerEntity}, excluding the entity itself. */
    public static void broadcastToVisiblePlayers(ProtoMessage<?> message, Entity centerEntity) {
        sendToClients(payload(message), packet -> {
            for (ServerPlayer player : PlayerLookup.tracking(centerEntity)) {
                player.connection.send(packet);
            }
        });
    }

    public static void broadcastToVisiblePlayersAndSelf(ProtoMessage<?> message, Player self) {
        broadcastToVisiblePlayersAndSelf(message, (Entity)self);
    }

    /** Forge {@code TRACKING_ENTITY_AND_SELF}: as above, plus the entity when it is a player. */
    public static void broadcastToVisiblePlayersAndSelf(ProtoMessage<?> message, Entity centerEntity) {
        sendToClients(payload(message), packet -> {
            for (ServerPlayer player : PlayerLookup.tracking(centerEntity)) {
                if (player != centerEntity) {
                    player.connection.send(packet);
                }
            }
            if (centerEntity instanceof ServerPlayer self) {
                self.connection.send(packet);
            }
        });
    }

    /** Sends an already-encoded clientbound frame to one player (used by the asset transfer port). */
    public static void sendFrame(ServerPlayer player, OutboundFrame frame) {
        player.connection.send(clientbound(frame));
    }

    private static Packet<?> clientbound(OutboundFrame frame) {
        return ServerPlayNetworking.createClientboundPacket(new YsmFramePayload(frame.bytes()));
    }

    private static Packet<?> serverbound(OutboundFrame frame) {
        return ClientPlayNetworking.createServerboundPacket(new YsmFramePayload(frame.bytes()));
    }

    /** One encoded clientbound packet, shared by every recipient (vanilla packets are immutable once built). */
    private static void sendToClients(NetworkPayload<?> payload, Consumer<Packet<?>> sender) {
        try (payload; var protobuf = ProtocolBuffer.serialize(payload.protobuf())) {
            var attachment =
                payload.raw().<UniBuffer>map(value -> value).orElseGet(() -> ArrayBuffer.borrow(new byte[0]));
            var spec = messageSpec(payload.protobuf(), MessageDirection.SERVER_TO_CLIENT);
            spec.attachmentPolicy().validate(attachment.size());
            try (var frame = FrameCodec.encode(spec.id(), protobuf, attachment)) {
                traceNetwork("outbound", "direction=" + MessageDirection.SERVER_TO_CLIENT,
                    "message=" + payload.protobuf().getClass().getSimpleName(), "protocolId=" + spec.id(),
                    "protobufBytes=" + protobuf.size(), "attachmentBytes=" + attachment.size(),
                    "frameBytes=" + frame.size());
                sender.accept(clientbound(frame));
            }
        }
    }

    private static ProtocolMessageSpec<?> messageSpec(ProtoMessage<?> message, MessageDirection expected) {
        var spec = ProtocolMessages.REGISTRY.find(message.getClass()).orElseThrow(
            () -> new IllegalArgumentException("Unregistered protocol message type: " + message.getClass().getName()));
        if (spec.direction() != expected) {
            throw new IllegalArgumentException("Protocol message has the wrong network direction");
        }
        return spec;
    }

    private static void receive(YsmFramePayload payload, MessageDirection direction, PacketContext context) {
        var bytes = payload.frame();
        traceNetwork("inbound", "direction=" + direction, "frameBytes=" + bytes.length);
        try {
            var source = new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes));
            var wire = com.elfmcys.ysm.buffer.SharedNativeBuffer.adopt(ForgeUniBufferIO.readNative(source, bytes.length));
            try (var frame = FrameCodec.decode(wire, id -> ProtocolInbound.accepts(id, direction))) {
                var spec = ProtocolMessages.REGISTRY.find(frame.messageId()).orElseThrow();
                ProtocolInbound.dispatch(frame, spec, () -> context);
            }
        } catch (RuntimeException error) {
            YesSteveModel.LOGGER.warn("Rejected invalid YSM frame", error);
            if (direction == MessageDirection.SERVER_TO_CLIENT) {
                var connection = context.getNetworkManager();
                context.enqueueWork(() -> {
                    try {
                        ClientSessionRuntime.failProtocolSession(connection);
                    } catch (IllegalStateException ignored) {
                    }
                });
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static NetworkPayload<?> payload(ProtoMessage<?> message) {
        return NetworkPayload.protobuf((ProtoMessage)message);
    }
}
