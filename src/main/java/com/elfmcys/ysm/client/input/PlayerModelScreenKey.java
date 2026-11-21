package com.elfmcys.ysm.client.input;

import com.elfmcys.ysm.platform.input.YsmKeyMapping;
import net.minecraft.client.input.KeyEvent;
import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.gui.ConfigScreen;
import com.elfmcys.ysm.client.gui.DisclaimerScreen;
import com.elfmcys.ysm.client.gui.PlayerModelScreen;
import com.elfmcys.ysm.config.ClientConfig;
import com.elfmcys.ysm.config.ServerConfig;
import com.elfmcys.ysm.network.NetworkHandler;
import com.elfmcys.ysm.util.InputCheckUtil;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public class PlayerModelScreenKey {
    public static final KeyMapping PLAYER_MODEL_KEY =
        new YsmKeyMapping("key.yes_steve_model.player_model.desc", YsmKeyMapping.Modifier.ALT, InputConstants.KEY_Y);

    public static void onKeyboardInput(KeyEvent event, int action) {
        if (!InputCheckUtil.isInGame()) {
            return;
        }
        if (action == InputConstants.PRESS && InputCheckUtil.keyIsMatch(event, PLAYER_MODEL_KEY)) {
            if (!YesSteveModel.isAvailable()) {
                YesSteveModel.sendUnavailableMessage();
                return;
            }
            if (NetworkHandler.isRemoteChannelPresent() && !ServerConfig.CAN_SWITCH_MODEL.get()) {
                Minecraft.getInstance().gui.setScreen(new ConfigScreen(null));
                return;
            }
            if (ClientConfig.DISCLAIMER_SHOW.get()) {
                Minecraft.getInstance().gui.setScreen(new DisclaimerScreen());
            } else {
                Minecraft.getInstance().gui.setScreen(new PlayerModelScreen());
            }
        }
    }
}
