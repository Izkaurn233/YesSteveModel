package com.elfmcys.ysm.client.compat;

import java.util.Optional;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Reports the absent-mod behavior of the excluded Forge integration. */
public final class CosmeticArmorCompat {
    private CosmeticArmorCompat() {}

    public static void init() {}

    public static boolean isInstalled() {
        return false;
    }

    public static Optional<ItemStack> getSkinArmorItem(Player player, EquipmentSlot slot) {
        return Optional.empty();
    }
}
