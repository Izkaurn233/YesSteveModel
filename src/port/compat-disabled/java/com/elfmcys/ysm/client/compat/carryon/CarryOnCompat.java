package com.elfmcys.ysm.client.compat.carryon;

import com.elfmcys.ysm.client.entity.CustomPlayerEntity;
import com.elfmcys.ysm.geckolib3.core.controller.IAnimationController;
import java.util.Optional;
import java.util.function.BiFunction;
import net.minecraft.world.entity.LivingEntity;

/** Reports the absent-mod behavior of the excluded Forge integration. */
public final class CarryOnCompat {
    private CarryOnCompat() {}

    public static void init() {}

    public static boolean isInstalled() {
        return false;
    }

    public static Optional<BiFunction<String, CustomPlayerEntity, IAnimationController<CustomPlayerEntity>>> animationPredicate() {
        return Optional.empty();
    }

    public static boolean isCarryOnPrincess(LivingEntity entity) {
        return false;
    }
}
