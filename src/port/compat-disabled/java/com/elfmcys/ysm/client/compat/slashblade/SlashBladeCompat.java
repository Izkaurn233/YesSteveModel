package com.elfmcys.ysm.client.compat.slashblade;

import com.elfmcys.ysm.client.entity.CustomHumanoidEntity;
import com.elfmcys.ysm.geckolib3.core.PlayState;
import com.elfmcys.ysm.geckolib3.core.builder.LoopType;
import com.elfmcys.ysm.geckolib3.core.event.predicate.AnimationEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Reports the absent-mod behavior of the excluded Forge integration. */
public final class SlashBladeCompat {
    private SlashBladeCompat() {}

    public static void init() {}

    public static boolean isSlashBladeLoaded() {
        return false;
    }

    public static boolean isSlashBladeItem(ItemStack stack) {
        return false;
    }

    public static String getAnimationName(AnimationEvent<? extends CustomHumanoidEntity<?>> event) {
        return "";
    }

    public static String getAnimationName(LivingEntity entity) {
        return "";
    }

    @Nullable
    public static PlayState playMainAnimation(LivingEntity entity,
        AnimationEvent<? extends CustomHumanoidEntity<?>> event, String animationName, LoopType loopType) {
        return null;
    }
}
