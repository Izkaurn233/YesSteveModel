package com.elfmcys.ysm.platform.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.resources.Identifier;

/**
 * Key mapping with the modifier requirement that Forge's {@code KeyModifier} provided.
 * <p>
 * PORT(26.3-fabric): vanilla 26.3 {@code KeyMapping} has no modifier or conflict context. YSM checks its keys
 * itself through {@link #matchesWithModifier(KeyEvent)} instead of {@code consumeClick()}, so a modifier
 * requirement is enforced here. Forge's {@code KeyConflictContext.IN_GAME} maps to the callers' existing
 * {@code InputCheckUtil.isInGame()} gate.
 * Known gap: the controls screen shows the bare key, not "Alt + key", and players cannot rebind the modifier.
 */
@Environment(EnvType.CLIENT)
public class YsmKeyMapping extends KeyMapping {
    public static final Category CATEGORY = Category.register(Identifier.fromNamespaceAndPath("ysm", "yes_steve_model"));

    private final Modifier modifier;

    public YsmKeyMapping(String name, Modifier modifier, int defaultKey) {
        super(name, InputConstants.Type.KEYBOARD, defaultKey, CATEGORY);
        this.modifier = modifier;
    }

    public Modifier modifier() {
        return modifier;
    }

    public boolean matchesWithModifier(KeyEvent event) {
        return matches(event) && modifier == Modifier.active(event.modifiers());
    }

    /** Mirrors Forge {@code KeyModifier}: exactly one modifier, or none, is considered active. */
    public enum Modifier {
        NONE(0),
        CONTROL(InputConstants.MOD_CONTROL),
        SHIFT(InputConstants.MOD_SHIFT),
        ALT(InputConstants.MOD_ALT);

        private final int mask;

        Modifier(int mask) {
            this.mask = mask;
        }

        /**
         * The first held modifier in CONTROL, SHIFT, ALT order, or NONE.
         * TODO: semantics not confirmed. Forge's precedence order when several modifiers are held was not checked
         * against Forge source.
         */
        public static Modifier active(int modifiers) {
            for (Modifier modifier : new Modifier[]{CONTROL, SHIFT, ALT}) {
                if ((modifiers & modifier.mask) != 0) {
                    return modifier;
                }
            }
            return NONE;
        }
    }
}
