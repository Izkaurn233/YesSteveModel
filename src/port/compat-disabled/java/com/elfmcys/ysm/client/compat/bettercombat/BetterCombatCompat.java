package com.elfmcys.ysm.client.compat.bettercombat;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;

/**
 * PORT(26.3-fabric) disabled stub: same name and API as the excluded Forge integration. It reports the mod as absent,
 * matching the original's not-installed branches. Delete when the real integration is ported.
 */
public class BetterCombatCompat {
    public static void init() {
    }

    public static boolean isInstalled() {
        return false;
    }

    public static String getAttackAnimation(AbstractClientPlayer player) {
        return "";
    }

    public static String attackAnimation(Entity entity) {
        return "";
    }
}
