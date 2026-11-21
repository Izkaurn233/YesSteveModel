package com.elfmcys.ysm.client.compat;

import com.elfmcys.ysm.client.entity.CustomVehicleEntity;
import com.elfmcys.ysm.geckolib3.core.event.predicate.AnimationEvent;
import java.util.Optional;
import org.joml.Vector3f;

/** Reports the absent-mod behavior of the excluded Forge integration. */
public final class ImmersiveAircraftCompat {
    private ImmersiveAircraftCompat() {}

    public static void init() {}

    public static Optional<Vector3f> getRotation(AnimationEvent<CustomVehicleEntity> event) {
        return Optional.empty();
    }
}
