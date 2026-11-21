package com.elfmcys.ysm.platform.input;

import java.util.Arrays;

/**
 * Translates Minecraft 26.x input codes back to the GLFW codes that model-facing contracts were written against.
 * <p>
 * PORT(26.3-fabric): Minecraft 26.x replaced GLFW with SDL3. {@code InputConstants.KEY_*} are SDL scancodes
 * (A=4, SPACE=44, ...) and mouse buttons are 1-based (LEFT=1, MIDDLE=2, RIGHT=3). Model packs pass GLFW key codes
 * to Molang {@code keyboard(...)} (SPACE=32, A=65, ...), so input state stays indexed by GLFW code.
 * SDL values come from the 26.3 {@code InputConstants$Type} key table (decompiled 2026-11-21); GLFW values are the
 * public LWJGL {@code GLFW_KEY_*} / {@code GLFW_MOUSE_BUTTON_*} constants.
 */
public final class GlfwKeyCompat {
    /** {@code GLFW_KEY_SPACE}: the first printable GLFW key code. */
    public static final int GLFW_KEY_FIRST = 32;
    /** {@code GLFW_KEY_LAST}. */
    public static final int GLFW_KEY_LAST = 348;
    /** {@code GLFW_KEY_UNKNOWN}. */
    public static final int GLFW_KEY_UNKNOWN = -1;
    /** {@code GLFW_MOUSE_BUTTON_LAST} ({@code GLFW_MOUSE_BUTTON_8}). */
    public static final int GLFW_MOUSE_BUTTON_LAST = 7;

    private static final int SDL_SCANCODE_LIMIT = 512;
    private static final int[] SDL_TO_GLFW = new int[SDL_SCANCODE_LIMIT];

    static {
        Arrays.fill(SDL_TO_GLFW, GLFW_KEY_UNKNOWN);
        // Letters: SDL a..z = 4..29, GLFW A..Z = 65..90.
        for (int i = 0; i < 26; i++) {
            map(4 + i, 65 + i);
        }
        // Digits: SDL 1..9 = 30..38 and 0 = 39; GLFW 0..9 = 48..57.
        for (int i = 1; i <= 9; i++) {
            map(29 + i, 48 + i);
        }
        map(39, 48);
        map(40, 257); // enter
        map(41, 256); // escape
        map(42, 259); // backspace
        map(43, 258); // tab
        map(44, 32); // space
        map(45, 45); // minus
        map(46, 61); // equal
        map(47, 91); // left bracket
        map(48, 93); // right bracket
        map(49, 92); // backslash
        map(50, 162); // world.2 (non-US hash)
        map(51, 59); // semicolon
        map(52, 39); // apostrophe
        map(53, 96); // grave accent
        map(54, 44); // comma
        map(55, 46); // period
        map(56, 47); // slash
        map(57, 280); // caps lock
        // F1..F12: SDL 58..69, GLFW 290..301.
        for (int i = 0; i < 12; i++) {
            map(58 + i, 290 + i);
        }
        map(70, 283); // print screen
        map(71, 281); // scroll lock
        map(72, 284); // pause
        map(73, 260); // insert
        map(74, 268); // home
        map(75, 266); // page up
        map(76, 261); // delete
        map(77, 269); // end
        map(78, 267); // page down
        map(79, 262); // right
        map(80, 263); // left
        map(81, 264); // down
        map(82, 265); // up
        map(83, 282); // num lock
        map(84, 331); // keypad divide
        map(85, 332); // keypad multiply
        map(86, 333); // keypad subtract
        map(87, 334); // keypad add
        map(88, 335); // keypad enter
        // Keypad 1..9: SDL 89..97, GLFW 321..329; keypad 0: SDL 98, GLFW 320.
        for (int i = 1; i <= 9; i++) {
            map(88 + i, 320 + i);
        }
        map(98, 320);
        map(99, 330); // keypad period (GLFW_KEY_KP_DECIMAL)
        map(100, 161); // world.1 (non-US backslash)
        map(101, 348); // application (GLFW_KEY_MENU)
        map(103, 336); // keypad equal
        // F13..F24: SDL 104..115, GLFW 302..313.
        for (int i = 0; i < 12; i++) {
            map(104 + i, 302 + i);
        }
        map(224, 341); // left control
        map(225, 340); // left shift
        map(226, 342); // left alt
        map(227, 343); // left super
        map(228, 345); // right control
        map(229, 344); // right shift
        map(230, 346); // right alt
        map(231, 347); // right super
    }

    private GlfwKeyCompat() {
    }

    private static void map(int sdlScancode, int glfwKey) {
        SDL_TO_GLFW[sdlScancode] = glfwKey;
    }

    /** GLFW key code for a 26.x keyboard value, or {@link #GLFW_KEY_UNKNOWN} when GLFW has no such key. */
    public static int toGlfwKey(int sdlScancode) {
        return sdlScancode >= 0 && sdlScancode < SDL_SCANCODE_LIMIT ? SDL_TO_GLFW[sdlScancode] : GLFW_KEY_UNKNOWN;
    }

    /**
     * GLFW mouse button for a 26.x mouse button value, or -1 when out of the GLFW range.
     * 26.x: LEFT=1, MIDDLE=2, RIGHT=3, 4..8. GLFW: LEFT=0, RIGHT=1, MIDDLE=2, 4th..8th = 3..7.
     */
    public static int toGlfwMouseButton(int button) {
        return switch (button) {
            case 1 -> 0;
            case 3 -> 1;
            case 2 -> 2;
            case 4, 5, 6, 7, 8 -> button - 1;
            default -> -1;
        };
    }
}
