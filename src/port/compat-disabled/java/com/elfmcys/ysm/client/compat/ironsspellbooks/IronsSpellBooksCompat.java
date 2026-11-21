package com.elfmcys.ysm.client.compat.ironsspellbooks;

import com.elfmcys.ysm.client.entity.CustomHumanoidEntity;
import com.elfmcys.ysm.geckolib3.core.PlayState;
import com.elfmcys.ysm.geckolib3.core.event.predicate.AnimationEvent;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/** Reports the absent-mod behavior of the excluded Forge integration. */
public final class IronsSpellBooksCompat {
    private IronsSpellBooksCompat() {}

    public static void init() {}

    @Nullable
    public static PlayState playAnimation(AnimationEvent<CustomHumanoidEntity<?>> event, LivingEntity entity) {
        return null;
    }
}
