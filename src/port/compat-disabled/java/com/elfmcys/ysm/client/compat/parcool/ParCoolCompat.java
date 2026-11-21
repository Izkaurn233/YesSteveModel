package com.elfmcys.ysm.client.compat.parcool;

import org.jetbrains.annotations.Nullable;
import com.elfmcys.ysm.client.entity.CustomPlayerEntity;
import com.elfmcys.ysm.geckolib3.core.controller.IAnimationController;
import java.util.Optional;
import java.util.function.BiFunction;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.tuple.Pair;

/** Reports the absent-mod behavior of the excluded Forge integration. */
public final class ParCoolCompat {
    private ParCoolCompat() {}

    public static void init() {}

    public static boolean isInstalled() {
        return false;
    }

    public static Optional<Pair<String, String>> getCompatibilityWarning() {
        return Optional.empty();
    }

    public static Optional<BiFunction<String, CustomPlayerEntity, IAnimationController<CustomPlayerEntity>>> animationPredicate() {
        return Optional.empty();
    }

    public static boolean hasAnimation(Player player) {
        return false;
    }

    @Nullable
    public static String getAnimation(Player player) {
        return null;
    }
}
