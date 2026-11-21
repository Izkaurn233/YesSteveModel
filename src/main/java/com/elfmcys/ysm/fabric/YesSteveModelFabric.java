package com.elfmcys.ysm.fabric;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.CapabilityBootstrap;
import com.elfmcys.ysm.event.CapabilityEvent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Fabric main entrypoint, replacing the Forge {@code @Mod} constructor.
 * <p>
 * PORT(26.3-fabric): common (both-side) handlers that Forge found through {@code @Mod.EventBusSubscriber} are
 * registered here as each group is ported. Remaining groups are tracked in PROJECT_MEMORY.md.
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
        registerCapabilityEvents();
    }

    private static void registerCapabilityEvents() {
        ServerPlayerEvents.COPY_FROM.register((original, clone, alive) -> CapabilityEvent.onPlayerCloned(original, clone));
        EntityTrackingEvents.START_TRACKING.register(CapabilityEvent::onTrackingPlayer);
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> CapabilityEvent.onEntityJoinWorld(entity));
        ServerTickEvents.END_SERVER_TICK.register(CapabilityEvent::onServerTick);
    }
}
