package com.elfmcys.ysm.client.compat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Reports the absent-mod behavior of the excluded Forge integration. */
public final class ElytraSlotCompat {
    private ElytraSlotCompat() {}

    public static void init() {}

    public static boolean isInstalled() {
        return false;
    }

    public static ItemStack getEquippedElytraItem(LivingEntity entity) {
        return ItemStack.EMPTY;
    }
}
