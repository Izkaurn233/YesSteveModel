package com.elfmcys.ysm.client.event;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.ClientStartup;
import com.elfmcys.ysm.model.ModelRuntime;
import com.elfmcys.ysm.client.gui.overlay.DebugAnimationScreen;
import com.elfmcys.ysm.client.gui.overlay.ExtraPlayerScreen;
import com.elfmcys.ysm.client.gui.overlay.LoadingStateScreen;
import com.elfmcys.ysm.client.input.AnimationRouletteKey;
import com.elfmcys.ysm.client.input.DebugAnimationKey;
import com.elfmcys.ysm.client.input.ExtraAnimationKey;
import com.elfmcys.ysm.client.input.ExtraPlayerConfigKey;
import com.elfmcys.ysm.client.input.PlayerModelScreenKey;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.DEBUG_TEXT;

// PORT(26.3-fabric): invoked from YesSteveModelFabricClient instead of the Forge mod bus.
public class ClientSetupEvent {
    // Forge ran this through FMLClientSetupEvent.enqueueWork on the main thread; Fabric client init is on the main thread too.
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

    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        event.registerAbove(DEBUG_TEXT.id(), "ysm_debug_info", DebugAnimationScreen.getGuiOverlay());
        event.registerAbove(DEBUG_TEXT.id(), "ysm_extra_player", new ExtraPlayerScreen());
        event.registerAbove(DEBUG_TEXT.id(), "ysm_loading_state", new LoadingStateScreen());
    }

}
