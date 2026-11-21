package com.elfmcys.ysm.util;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import com.elfmcys.ysm.platform.input.YsmKeyMapping;
import net.minecraft.client.input.KeyEvent;

public class InputCheckUtil {
    // PORT(26.3-fabric): Forge KeyModifier matching moved into YsmKeyMapping.
    public static boolean keyIsMatch(KeyEvent event, KeyMapping keyMapping) {
        if (keyMapping instanceof YsmKeyMapping ysmKey) {
            return ysmKey.matchesWithModifier(event);
        }
        return keyMapping.matches(event) && YsmKeyMapping.Modifier.active(event.modifiers()) == YsmKeyMapping.Modifier.NONE;
    }

    public static boolean isInGame() {
        Minecraft mc = Minecraft.getInstance();
        // 不能是加载界面
        if (mc.gui.overlay() != null) {
            return false;
        }
        // 不能打开任何 GUI
        if (mc.gui.screen() != null) {
            return false;
        }
        // 当前窗口捕获鼠标操作
        if (!mc.mouseHandler.isMouseGrabbed()) {
            return false;
        }
        // 选择了当前窗口
        return mc.isWindowActive();
    }
}
