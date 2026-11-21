package com.elfmcys.ysm.fabric;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.CapabilityBootstrap;
import com.elfmcys.ysm.event.CapabilityEvent;
import com.elfmcys.ysm.event.CommandRegistry;
import com.elfmcys.ysm.event.CommonEvent;
import com.elfmcys.ysm.event.LoggedOutEvent;
import com.elfmcys.ysm.event.LoginEvent;
import com.elfmcys.ysm.event.ServerStartingEvent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Fabric main entrypoint, replacing the Forge {@code @Mod} constructor.
 * PORT(26.3-fabric): common (both-side) handlers that Forge found through {@code @Mod.EventBusSubscriber} are
 */
public final class YesSteveModelFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CapabilityBootstrap.common();
        try {
            YesSteveModel.init();
        } catch (IOException exception) {
            throw new UncheckedIOException("YSM failed to initialize", exception);
        }
        CommonEvent.onSetup();
        registerCapabilityEvents();
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> CommandRegistry.register(dispatcher));
        ServerLifecycleEvents.SERVER_STARTING.register(ServerStartingEvent::onServerInit);
        ServerLifecycleEvents.SERVER_STOPPING.register(ServerStartingEvent::onServerStopping);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> LoginEvent.onLoggedInServer(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> LoggedOutEvent.onPlayerLoggedOut(handler.player));
    }

    private static void registerCapabilityEvents() {
        ServerPlayerEvents.COPY_FROM.register((original, clone, alive) -> CapabilityEvent.onPlayerCloned(original, clone));
        EntityTrackingEvents.START_TRACKING.register(CapabilityEvent::onTrackingPlayer);
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> CapabilityEvent.onEntityJoinWorld(entity));
        ServerTickEvents.END_SERVER_TICK.register(CapabilityEvent::onServerTick);
    }
}
