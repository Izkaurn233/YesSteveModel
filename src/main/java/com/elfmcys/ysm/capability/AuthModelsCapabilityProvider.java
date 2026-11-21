package com.elfmcys.ysm.capability;

import com.elfmcys.ysm.platform.capability.CapKey;
import com.elfmcys.ysm.platform.capability.NbtCapabilityCodec;

// PORT(26.3-fabric): the Forge ICapabilitySerializable is replaced by a persistent CapKey (Fabric data attachment).
public final class AuthModelsCapabilityProvider {
    public static final CapKey<AuthModelsCapability> AUTH_MODELS_CAP = CapKey.persistentKey("own_models",
        NbtCapabilityCodec.list(AuthModelsCapability::new, AuthModelsCapability::serializeNBT,
            AuthModelsCapability::deserializeNBT),
        CapabilityRules::playerModels, entity -> new AuthModelsCapability());

    private AuthModelsCapabilityProvider() {
    }
}
