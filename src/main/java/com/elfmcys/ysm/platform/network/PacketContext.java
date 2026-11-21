package com.elfmcys.ysm.platform.network;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * The part of Forge {@code NetworkEvent.Context} that YSM handlers use, with the same method names, so handlers only
 * change their import.
 * <p>
 * PORT(26.3-fabric): Fabric play payload handlers run on the game thread, and Forge ran them on the netty thread. So
 * {@link #enqueueWork} may already be on the game thread; it then runs the task immediately, which keeps ordering.
 * The owning executor still decides: {@code MinecraftServer} / {@code Minecraft} are reentrant event loops.
 */
public final class PacketContext {
    private final Connection connection;
    private final @Nullable ServerPlayer sender;
    private final Executor gameThread;

    public PacketContext(Connection connection, @Nullable ServerPlayer sender, Executor gameThread) {
        this.connection = Objects.requireNonNull(connection, "connection");
        this.sender = sender;
        this.gameThread = Objects.requireNonNull(gameThread, "gameThread");
    }

    /** The sending player for client-to-server packets; null for server-to-client packets. */
    public @Nullable ServerPlayer getSender() {
        return sender;
    }

    /** The exact connection the frame arrived on. */
    public Connection getNetworkManager() {
        return connection;
    }

    public CompletableFuture<Void> enqueueWork(Runnable work) {
        return CompletableFuture.runAsync(work, gameThread);
    }

    /** Forge bookkeeping only; Fabric needs no acknowledgement. */
    public void setPacketHandled(boolean handled) {
    }
}
