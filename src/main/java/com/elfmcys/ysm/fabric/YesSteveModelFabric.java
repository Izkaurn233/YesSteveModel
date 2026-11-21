package com.elfmcys.ysm.fabric;

import com.elfmcys.ysm.YesSteveModel;
import net.fabricmc.api.ModInitializer;

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
        try {
            YesSteveModel.init();
        } catch (IOException exception) {
            throw new UncheckedIOException("YSM failed to initialize", exception);
        }
    }
}
