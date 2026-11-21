package com.elfmcys.ysm.client.compat.immersivemelodies;

import net.minecraft.world.entity.LivingEntity;
/** Reports the absent-mod behavior of the excluded Forge integration. */
public final class ImmersiveMelodiesCompat {
    private ImmersiveMelodiesCompat() {}

    public static void init() {}

    public static boolean isLoaded() {
        return false;
    }

    public static void updateMelodyProgress(LivingEntity entity, ImmersiveMelodiesData reading) {}

    public static final class ImmersiveMelodiesData {
        public float pitch;
        public long delta;
        public long time;
    }
}
