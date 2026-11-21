package com.elfmcys.ysm.fabric.client;

import com.elfmcys.ysm.client.event.ClientSetupEvent;
import com.elfmcys.ysm.client.event.ModInputEvent;
import com.elfmcys.ysm.client.event.PlayerMoveEvent;
import com.elfmcys.ysm.client.input.AnimationRouletteKey;
import com.elfmcys.ysm.client.input.DebugAnimationKey;
import com.elfmcys.ysm.client.input.ExtraAnimationKey;
import com.elfmcys.ysm.client.input.ExtraPlayerConfigKey;
import com.elfmcys.ysm.client.input.PlayerModelScreenKey;
import com.elfmcys.ysm.platform.input.ClientInputHooks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
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
        ClientSetupEvent.onRegisterKeyMappings();
        registerInput();
        registerTicks();
        ClientSetupEvent.onClientSetup();
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
