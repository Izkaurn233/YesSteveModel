package com.elfmcys.ysm.capability;

import com.elfmcys.ysm.platform.capability.CapKey;
import com.elfmcys.ysm.platform.capability.NbtCapabilityCodec;

// PORT(26.3-fabric): the Forge ICapabilitySerializable is replaced by a persistent CapKey (Fabric data attachment).
public final class StarModelsCapabilityProvider {
    public static final CapKey<StarModelsCapability> STAR_MODELS_CAP = CapKey.persistentKey("star_models",
        NbtCapabilityCodec.list(StarModelsCapability::new, StarModelsCapability::serializeNBT,
            StarModelsCapability::deserializeNBT),
        CapabilityRules::playerModels, entity -> new StarModelsCapability());

    private StarModelsCapabilityProvider() {
    }
}
