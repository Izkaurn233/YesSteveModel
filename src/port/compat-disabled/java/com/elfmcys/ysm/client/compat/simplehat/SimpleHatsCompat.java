package com.elfmcys.ysm.client.compat.simplehat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Reports the absent-mod behavior of the excluded Forge integration. */
public final class SimpleHatsCompat {
    private SimpleHatsCompat() {}

    public static void init() {}

    @Nullable
    public static ItemStack getCuriosHead(LivingEntity entity) {
        return null;
    }
}
