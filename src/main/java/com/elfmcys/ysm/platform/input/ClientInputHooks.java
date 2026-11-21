package com.elfmcys.ysm.platform.input;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Raw input dispatch that replaces Forge {@code InputEvent.Key} and {@code InputEvent.MouseButton.Post}.
 * <p>
 * PORT(26.3-fabric): fed by {@code KeyboardHandlerMixin} and {@code MouseHandlerMixin} after vanilla has handled
 * the input, the same point where Forge posted its events. Values are 26.x native: SDL scancodes and 1-based mouse
 * buttons. Use {@link GlfwKeyCompat} where a GLFW-coded contract is needed.
 */
@Environment(EnvType.CLIENT)
public final class ClientInputHooks {
    @FunctionalInterface
    public interface KeyListener {
        void onKey(KeyEvent event, int action);
    }

    @FunctionalInterface
    public interface MouseListener {
        void onMouseButton(MouseButtonInfo button, int action);
    }

    private static final List<KeyListener> KEY_LISTENERS = new CopyOnWriteArrayList<>();
    private static final List<MouseListener> MOUSE_LISTENERS = new CopyOnWriteArrayList<>();

    private ClientInputHooks() {
    }

    public static void onKey(KeyListener listener) {
        KEY_LISTENERS.add(listener);
    }

    public static void onMouseButton(MouseListener listener) {
        MOUSE_LISTENERS.add(listener);
    }

    public static void fireKey(KeyEvent event, int action) {
        for (KeyListener listener : KEY_LISTENERS) {
            listener.onKey(event, action);
        }
    }

    public static void fireMouseButton(MouseButtonInfo button, int action) {
        for (MouseListener listener : MOUSE_LISTENERS) {
            listener.onMouseButton(button, action);
        }
    }
}
