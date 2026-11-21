package com.elfmcys.ysm.client.input;

import com.elfmcys.ysm.platform.input.YsmKeyMapping;
import net.minecraft.client.input.KeyEvent;
import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.gui.ExtraPlayerConfigScreen;
import com.elfmcys.ysm.util.InputCheckUtil;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public class ExtraPlayerConfigKey {
    public static final KeyMapping EXTRA_PLAYER_RENDER_KEY =
        new YsmKeyMapping("key.yes_steve_model.open_extra_player_render.desc", YsmKeyMapping.Modifier.ALT, InputConstants.KEY_P);

    public static void onKeyboardInput(KeyEvent event, int action) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (!InputCheckUtil.isInGame()) {
            return;
        }
        if (action == InputConstants.PRESS && InputCheckUtil.keyIsMatch(event, EXTRA_PLAYER_RENDER_KEY)) {
            Minecraft.getInstance().gui.setScreen(new ExtraPlayerConfigScreen());
        }
    }
}
