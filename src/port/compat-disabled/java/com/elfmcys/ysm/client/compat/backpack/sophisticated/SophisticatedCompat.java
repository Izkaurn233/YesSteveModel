package com.elfmcys.ysm.client.compat.backpack.sophisticated;

import org.jetbrains.annotations.Nullable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;

/** Neutral result for the excluded Forge integration. */
public final class SophisticatedCompat {
    private SophisticatedCompat() {}

    public static void init() {}

    public static boolean isInstalled() {
        return false;
    }

    public static Optional<Pair<String, String>> getCompatibilityWarning() {
        return Optional.empty();
    }

    public static void addLayer() {}

    @Nullable
    public static ItemStack getBackpackItemStack(LivingEntity entity) {
        return null;
    }
}
