package com.elfmcys.ysm.client.compat.swem;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/** Reports the absent-mod behavior of the excluded Forge integration. */
public final class SwemCompat {
    private SwemCompat() {}

    public static void init() {}

    @Nullable
    public static String getAnimation(LivingEntity entity) {
        return null;
    }
}
