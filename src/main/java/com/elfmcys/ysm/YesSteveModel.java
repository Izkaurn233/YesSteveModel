package com.elfmcys.ysm;

import com.elfmcys.ysm.api.event.Event;
import com.elfmcys.ysm.api.event.EventManager;
import com.elfmcys.ysm.api.internal.event.ExtensionEventHandlerLoader;
import com.elfmcys.ysm.config.ClientConfig;
import com.elfmcys.ysm.config.ServerConfig;
import com.elfmcys.ysm.init.ModSounds;
import com.elfmcys.ysm.platform.Platform;
import com.elfmcys.ysm.util.NativeLibUtil;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Process-wide YSM entry state.
 * <p>
 * PORT(26.3-fabric): no longer a Forge {@code @Mod} class. {@code fabric.YesSteveModelFabric} calls {@link #init()}
 * from the Fabric main entrypoint, and the Forge mod event bus is replaced by {@link #EVENTS}.
 */
public final class YesSteveModel {
    public static final String MOD_ID = "ysm";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    /** Bus for YSM's public events (locators, render model/layer, special player render). */
    public static final EventManager EVENTS = new EventManager();
    public static ModContainer MOD;

    private YesSteveModel() {
    }

    /** Runs once from the Fabric main entrypoint, on both physical sides. */
    public static void init() throws IOException {
        MOD = Platform.ysm();
        NativeLibUtil.load();
        // Platform detection must finish before platform-dependent defaults are registered.
        initConfig();
        if (!NativeLibUtil.isAvailable()) {
            // Fabric has no mod-loading warning screen; the client also gets a chat message on join.
            LOGGER.error(getUnavailableMessageString());
            return;
        }

        ExtensionEventHandlerLoader.registerAll(EVENTS);
    }

    public static void registerEventHandler(Object handler) {
        EVENTS.register(handler);
    }

    private static void initConfig() {
        // PORT(26.3-fabric): Forge Config API Port replaces ModLoadingContext.registerConfig. Same spec classes.
        // TODO: not verified: config file names and SERVER-config sync behaviour versus Forge 1.20.1.
        ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.CLIENT, ClientConfig.init());
        ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.SERVER, ServerConfig.init());
        if (Platform.isClient()) {
            ModSounds.register();
        }
    }

    /**
     * Posts a YSM event.
     *
     * @return {@code true} when the event was cancelled, the same contract as the former Forge bus post
     */
    public static boolean postEvent(Event event) {
        return EVENTS.post(event);
    }

    public static boolean isAvailable() {
        return NativeLibUtil.isAvailable();
    }

    public static boolean isMobilePlatform() {
        return NativeLibUtil.isMobilePlatform();
    }

    @Environment(EnvType.CLIENT)
    public static void sendUnavailableMessage() {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            player.sendSystemMessage(getUnavailableMessage());
        }
    }

    public static Component getUnavailableMessage() {
        return NativeLibUtil.getUnsupportedMsg();
    }

    public static String getUnavailableMessageString() {
        return NativeLibUtil.getUnsupportedMsgStr();
    }
}
