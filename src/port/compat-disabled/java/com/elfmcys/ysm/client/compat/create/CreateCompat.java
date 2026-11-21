package com.elfmcys.ysm.client.compat.create;

import net.minecraft.world.entity.player.Player;

/** Reports the absent-mod behavior of the excluded Forge integration. */
public final class CreateCompat {
    private CreateCompat() {}

    public static void init() {}

    public static boolean isInstalled() {
        return false;
    }

    public static boolean isHangingSkyhook(Player player) {
        return false;
    }
}
