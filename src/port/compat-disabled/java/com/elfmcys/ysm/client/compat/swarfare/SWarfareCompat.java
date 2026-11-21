package com.elfmcys.ysm.client.compat.swarfare;

import com.elfmcys.ysm.client.entity.CustomHumanoidEntity;
import com.elfmcys.ysm.geckolib3.core.PlayState;
import com.elfmcys.ysm.geckolib3.core.builder.LoopType;
import com.elfmcys.ysm.geckolib3.core.event.predicate.AnimationEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Reports the absent-mod behavior of the excluded Forge integration. */
public final class SWarfareCompat {
    private SWarfareCompat() {}

    public static void init() {}

    public static boolean isInstalled() {
        return false;
    }

    @Nullable
    public static Identifier getGunId(ItemStack stack) {
        return null;
    }

    @Nullable
    public static PlayState playGunMainAnimation(LivingEntity entity,
        AnimationEvent<? extends CustomHumanoidEntity<? extends LivingEntity>> event, String animationName, LoopType loopType) {
        return null;
    }

    @Nullable
    public static PlayState playGunHoldAnimation(ItemStack stack,
        AnimationEvent<? extends CustomHumanoidEntity<? extends LivingEntity>> event) {
        return null;
    }

    @Nullable
    public static PlayState playGunOnceAnimation(ItemStack stack,
        AnimationEvent<? extends CustomHumanoidEntity<? extends LivingEntity>> event) {
        return null;
    }

    public static boolean isGun(ItemStack stack) {
        return false;
    }

    public static boolean shouldHidePlayerRender(Player player) {
        return false;
    }
}
