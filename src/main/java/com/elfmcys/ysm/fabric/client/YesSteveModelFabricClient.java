package com.elfmcys.ysm.fabric.client;

import com.elfmcys.ysm.client.event.ClientCommandHandler;
import com.elfmcys.ysm.client.event.ClientLoggedEvent;
import com.elfmcys.ysm.client.event.ClientSetupEvent;
import com.elfmcys.ysm.client.event.EntityLoadEvent;
import com.elfmcys.ysm.client.event.ModInputEvent;
import com.elfmcys.ysm.client.event.PlayerMoveEvent;
import com.elfmcys.ysm.client.event.VanillaPlayerRenderEvent;
import com.elfmcys.ysm.event.api.SpecialPlayerRenderEvent;
import com.elfmcys.ysm.client.gui.GuiModelPreview;
import com.elfmcys.ysm.client.input.AnimationRouletteKey;
import com.elfmcys.ysm.client.input.DebugAnimationKey;
import com.elfmcys.ysm.client.input.ExtraAnimationKey;
import com.elfmcys.ysm.client.input.ExtraPlayerConfigKey;
import com.elfmcys.ysm.client.input.PlayerModelScreenKey;
import com.elfmcys.ysm.platform.input.ClientInputHooks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Fabric client entrypoint. It replaces the Forge {@code @Mod.EventBusSubscriber(value = Dist.CLIENT)} scan by
 * registering every client handler explicitly.
 * <p>
 * PORT(26.3-fabric): grows as each handler group is ported. Remaining groups are tracked in PROJECT_MEMORY.md.
 */
@Environment(EnvType.CLIENT)
public final class YesSteveModelFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        com.elfmcys.ysm.capability.CapabilityBootstrap.client();
        if (com.elfmcys.ysm.YesSteveModel.isAvailable()) {
            com.elfmcys.ysm.network.NetworkHandler.initClient();
            com.elfmcys.ysm.YesSteveModel.EVENTS.addListener(SpecialPlayerRenderEvent.class,
                VanillaPlayerRenderEvent::onRenderPlayer);
        }
        ClientSetupEvent.onRegisterKeyMappings();
        ClientSetupEvent.onRegisterGuiOverlays();
        PictureInPictureRendererRegistry.register(context -> new GuiModelPreview.Renderer());
        registerInput();
        registerTicks();
        registerClientEvents();
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> ClientSetupEvent.onClientSetup());
    }

    private static void registerClientEvents() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> ClientCommandHandler.register(dispatcher));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ClientLoggedEvent.onPlayerLoggedIn(handler.getConnection()));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientLoggedEvent.onPlayerLoggedOut(handler.getConnection()));
        ClientEntityEvents.ENTITY_LOAD.register((entity, level) -> EntityLoadEvent.onEntityLoadToWorld(entity));
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, level) -> EntityLoadEvent.onEntityLeaveWorld(entity));
    }

    private static void registerTicks() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> com.elfmcys.ysm.client.event.ClientTickEvent.onClientTick());
        ClientTickEvents.END_CLIENT_TICK.register(client -> PlayerMoveEvent.onClientTick());
    }

    private static void registerInput() {
        ClientInputHooks.onKey(ModInputEvent::onKeyInput);
        ClientInputHooks.onMouseButton(ModInputEvent::onMouseInput);
        ClientInputHooks.onKey(PlayerMoveEvent::onKeyboardInput);
        ClientInputHooks.onKey(AnimationRouletteKey::onKeyboardInput);
        ClientInputHooks.onKey(DebugAnimationKey::onKeyboardInput);
        ClientInputHooks.onKey(ExtraAnimationKey::onKeyboardInput);
        ClientInputHooks.onKey(ExtraPlayerConfigKey::onKeyboardInput);
        ClientInputHooks.onKey(PlayerModelScreenKey::onKeyboardInput);
    }
}
