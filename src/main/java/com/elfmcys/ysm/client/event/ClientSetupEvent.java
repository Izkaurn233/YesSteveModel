package com.elfmcys.ysm.client.event;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.ClientStartup;
import com.elfmcys.ysm.model.ModelRuntime;
import com.elfmcys.ysm.client.gui.overlay.DebugAnimationScreen;
import com.elfmcys.ysm.client.gui.overlay.LoadingStateScreen;
import com.elfmcys.ysm.client.gui.overlay.ExtraPlayerScreen;
import com.elfmcys.ysm.client.input.AnimationRouletteKey;
import com.elfmcys.ysm.client.input.DebugAnimationKey;
import com.elfmcys.ysm.client.input.ExtraAnimationKey;
import com.elfmcys.ysm.client.input.ExtraPlayerConfigKey;
import com.elfmcys.ysm.client.input.PlayerModelScreenKey;
import net.minecraft.resources.Identifier;

// PORT(26.3-fabric): invoked from YesSteveModelFabricClient instead of the Forge mod bus.
public class ClientSetupEvent {
    // Required model publication uses the initialized client render services.
    public static void onClientSetup() {
        if (!YesSteveModel.isAvailable()) {
            return;
        }

        ClientStartup.start(ModelRuntime.system());
    }

    public static void onRegisterKeyMappings() {
        KeyMappingHelper.registerKeyMapping(PlayerModelScreenKey.PLAYER_MODEL_KEY);

        if (!YesSteveModel.isAvailable()) {
            return;
        }

        KeyMappingHelper.registerKeyMapping(AnimationRouletteKey.ANIMATION_ROULETTE_KEY);
        KeyMappingHelper.registerKeyMapping(AnimationRouletteKey.LOCK_ROULETTE_KEY);
        KeyMappingHelper.registerKeyMapping(DebugAnimationKey.DEBUG_ANIMATION_KEY);
        KeyMappingHelper.registerKeyMapping(ExtraPlayerConfigKey.EXTRA_PLAYER_RENDER_KEY);
        ExtraAnimationKey.registerKeyBinding();
    }

    public static void onRegisterGuiOverlays() {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        var debugInfo = Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "debug_info");
        HudElementRegistry.attachElementAfter(VanillaHudElements.DEMO_TIMER, debugInfo, DebugAnimationScreen.getGuiOverlay());
        var extraPlayer = Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "extra_player");
        HudElementRegistry.attachElementAfter(debugInfo, extraPlayer, new ExtraPlayerScreen());
        HudElementRegistry.attachElementAfter(extraPlayer,
            Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "loading_state"), new LoadingStateScreen());
    }

}
