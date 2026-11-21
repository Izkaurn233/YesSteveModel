package com.elfmcys.ysm.client.compat.tacz;

import com.elfmcys.ysm.client.entity.CustomHumanoidEntity;
import com.elfmcys.ysm.geckolib3.core.PlayState;
import com.elfmcys.ysm.geckolib3.core.builder.LoopType;
import com.elfmcys.ysm.geckolib3.core.event.predicate.AnimationEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Reports the absent-mod behavior of the excluded Forge integration. */
public final class TACZCompat {
    private TACZCompat() {}

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
        AnimationEvent<? extends CustomHumanoidEntity<?>> event, String animationName, LoopType loopType) {
        return null;
    }

    @Nullable
    public static PlayState playGunHoldAnimation(ItemStack stack,
        AnimationEvent<? extends CustomHumanoidEntity<?>> event) {
        return null;
    }

    @Nullable
    public static PlayState playGunOnceAnimation(ItemStack stack,
        AnimationEvent<? extends CustomHumanoidEntity<?>> event) {
        return null;
    }

    public static void openFlashShellRender(LivingEntity entity, ItemStack mainHandItem) {}

    public static void stopFlashShellRender(ItemStack mainHandItem) {}

    public static boolean isMainhandHoldingGun(LivingEntity entity) {
        return false;
    }
}
