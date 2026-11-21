package com.elfmcys.ysm.capability;

import com.elfmcys.ysm.platform.capability.CapKey;
import com.elfmcys.ysm.platform.capability.NbtCapabilityCodec;

// PORT(26.3-fabric): the Forge ICapabilitySerializable is replaced by a persistent CapKey (Fabric data attachment).
public final class VehicleModelInfoCapabilityProvider {
    public static final CapKey<VehicleModelInfoCapability> CAP = CapKey.persistentKey("vehicle_model_id",
        NbtCapabilityCodec.compound(VehicleModelInfoCapability::new, VehicleModelInfoCapability::serializeNBT,
            VehicleModelInfoCapability::deserializeNBT),
        CapabilityRules::vehicleModelInfo, entity -> new VehicleModelInfoCapability());

    private VehicleModelInfoCapabilityProvider() {
    }
}
