package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.platform.input.GlfwKeyCompat;
import com.elfmcys.ysm.util.InputCheckUtil;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;

/**
 * Held-key state for model Molang. Both arrays stay indexed by GLFW codes, the codes model packs were written against.
 * PORT(26.3-fabric): input arrives as SDL scancodes and 1-based buttons and is translated by {@link GlfwKeyCompat}.
 * Registered from {@code YesSteveModelFabricClient}.
 */
public class ModInputEvent {
    public static volatile boolean[] KEY_STATES = new boolean[GlfwKeyCompat.GLFW_KEY_LAST + 1];
    public static volatile boolean[] MOUSE_STATES = new boolean[GlfwKeyCompat.GLFW_MOUSE_BUTTON_LAST + 1];

    public static void onKeyInput(KeyEvent event, int action) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (!InputCheckUtil.isInGame()) {
            return;
        }
        int key = GlfwKeyCompat.toGlfwKey(event.key());
        if (GlfwKeyCompat.GLFW_KEY_FIRST <= key && key <= GlfwKeyCompat.GLFW_KEY_LAST) {
            if (action == InputConstants.PRESS) {
                KEY_STATES[key] = true;
            } else if (action == InputConstants.RELEASE) {
                KEY_STATES[key] = false;
            }
        }
    }

    public static void onMouseInput(MouseButtonInfo info, int action) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (!InputCheckUtil.isInGame()) {
            return;
        }
        int button = GlfwKeyCompat.toGlfwMouseButton(info.button());
        if (0 <= button && button <= GlfwKeyCompat.GLFW_MOUSE_BUTTON_LAST) {
            if (action == InputConstants.PRESS) {
                MOUSE_STATES[button] = true;
            } else if (action == InputConstants.RELEASE) {
                MOUSE_STATES[button] = false;
            }
        }
    }
}
