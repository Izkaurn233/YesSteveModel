package com.elfmcys.ysm.platform;

import com.elfmcys.ysm.YesSteveModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Loader facts that Forge exposed through {@code FMLEnvironment}, {@code FMLPaths} and {@code ModList}.
 * <p>
 * PORT(26.3-fabric): single seam for loader queries, so the rest of the code does not import Fabric Loader directly.
 */
public final class Platform {
    private Platform() {
    }

    /** Replaces {@code FMLEnvironment.dist == Dist.CLIENT}. */
    public static boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    /** Replaces {@code FMLEnvironment.dist == Dist.DEDICATED_SERVER}. */
    public static boolean isDedicatedServer() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
    }

    /** Replaces {@code !FMLEnvironment.production}. */
    public static boolean isDevelopment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    /** Replaces {@code FMLPaths.GAMEDIR.get()}. */
    public static Path gameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    /** Replaces {@code FMLPaths.CONFIGDIR.get()}. */
    public static Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    /** Replaces {@code ModList.get().isLoaded(id)} and {@code LoadingModList.get().getModFileById(id) != null}. */
    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    public static Optional<ModContainer> mod(String modId) {
        return FabricLoader.getInstance().getModContainer(modId);
    }

    /** Version string of an installed mod; empty when the mod is absent. */
    public static Optional<String> modVersion(String modId) {
        return mod(modId).map(container -> container.getMetadata().getVersion().getFriendlyString());
    }

    /** Replaces {@code ModList.get().getModFileById(MOD_ID).versionString()}. */
    public static String ysmVersion() {
        return modVersion(YesSteveModel.MOD_ID).orElse("unknown");
    }

    public static ModContainer ysm() {
        return mod(YesSteveModel.MOD_ID)
                .orElseThrow(() -> new IllegalStateException("YSM mod container is not registered"));
    }
}
