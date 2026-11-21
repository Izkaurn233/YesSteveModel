package com.elfmcys.ysm.capability;

import com.elfmcys.ysm.platform.capability.CapKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Holder of a client vehicle animatable, initialised on demand by the network layer.
 * PORT(26.3-fabric): {@link #CAP} is a derived key, present only once {@link #initialize()} has run, as in Forge.
 */
@Environment(EnvType.CLIENT)
public class VehicleAnimatableCapabilityProvider {
    public static final CapKey<VehicleAnimatableCapability> CAP = CapKey.derivedKey("vehicle_animatable", entity -> {
        var lazy = ClientLazyCapabilityProvider.CAP.peek(entity);
        return lazy == null ? null : lazy.getVehicleAnimatableCapabilityProvider().instance();
    });

    private VehicleAnimatableCapability instance;
    private Entity entity;

    public VehicleAnimatableCapabilityProvider(Entity entity) {
        this.entity = entity;
    }

    public VehicleAnimatableCapability initialize() {
        if (this.instance == null) {
            this.instance = new VehicleAnimatableCapability(entity);
            this.entity = null;
        }
        return instance;
    }

    public @Nullable VehicleAnimatableCapability instance() {
        return instance;
    }
}
