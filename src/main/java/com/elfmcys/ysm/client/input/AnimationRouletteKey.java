package com.elfmcys.ysm.client.input;

import com.elfmcys.ysm.platform.input.YsmKeyMapping;
import net.minecraft.client.input.KeyEvent;
import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.client.gui.AnimationRouletteScreen;
import com.elfmcys.ysm.config.ServerConfig;
import com.elfmcys.ysm.network.NetworkHandler;
import com.elfmcys.ysm.util.InputCheckUtil;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public class AnimationRouletteKey {
    public static final KeyMapping ANIMATION_ROULETTE_KEY =
        new YsmKeyMapping("key.yes_steve_model.animation_roulette.desc", YsmKeyMapping.Modifier.NONE, InputConstants.KEY_Z);

    public static final KeyMapping LOCK_ROULETTE_KEY =
        new YsmKeyMapping("key.yes_steve_model.lock_roulette.desc", YsmKeyMapping.Modifier.ALT, InputConstants.KEY_L);

    public static void onKeyboardInput(KeyEvent event, int action) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (!InputCheckUtil.isInGame()) {
            return;
        }
        if (action == InputConstants.PRESS && InputCheckUtil.keyIsMatch(event, ANIMATION_ROULETTE_KEY)
            && (!NetworkHandler.isRemoteChannelPresent() || ServerConfig.CAN_SWITCH_MODEL.get())) {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.getCapability(PlayerAnimatableCapabilityProvider.CAP).ifPresent(cap -> {
                    String modelId = cap.getModelId();
                    var model = cap.getModelRenderTarget();
                    if (model != null && !model.info().getExtraAnimations().isEmpty()) {
                        if (Minecraft.getInstance().gui.screen() == null) {
                            Minecraft.getInstance().gui.setScreen(new AnimationRouletteScreen(modelId, model, cap));
                            return;
                        }
                        if (Minecraft.getInstance().gui.screen() instanceof AnimationRouletteScreen) {
                            Minecraft.getInstance().gui.setScreen(null);
                        }
                    }
                });
            }
        }
    }
}
