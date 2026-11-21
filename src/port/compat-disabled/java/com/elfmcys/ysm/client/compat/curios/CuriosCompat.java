package com.elfmcys.ysm.client.compat.curios;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

/** Reports the absent-mod behavior of the excluded Forge integration. */
public final class CuriosCompat {
    private CuriosCompat() {}

    public static void init() {}

    public static boolean isInstalled() {
        return false;
    }

    public static boolean hasAnyItemEquipped(LivingEntity entity, String slotType, ReferenceOpenHashSet<Item> items) {
        return false;
    }
}
