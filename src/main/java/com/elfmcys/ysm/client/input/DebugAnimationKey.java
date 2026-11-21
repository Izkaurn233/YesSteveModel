package com.elfmcys.ysm.client.input;

import com.elfmcys.ysm.platform.input.YsmKeyMapping;
import net.minecraft.client.input.KeyEvent;
import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.gui.overlay.DebugAnimationScreen;
import com.elfmcys.ysm.util.InputCheckUtil;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public class DebugAnimationKey {
    public static final KeyMapping DEBUG_ANIMATION_KEY =
        new YsmKeyMapping("key.yes_steve_model.debug_animation.desc", YsmKeyMapping.Modifier.ALT, InputConstants.KEY_B);

    public static void onKeyboardInput(KeyEvent event, int action) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (!InputCheckUtil.isInGame()) {
            return;
        }
        if (action == InputConstants.PRESS && InputCheckUtil.keyIsMatch(event, DEBUG_ANIMATION_KEY)) {
            if (!DebugAnimationScreen.isEnabled()) {
                DebugAnimationScreen.enable();
            } else {
                DebugAnimationScreen.disable();
            }
        }
    }
}
