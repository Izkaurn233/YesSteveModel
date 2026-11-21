package com.elfmcys.ysm.capability;

import com.elfmcys.ysm.platform.capability.CapKey;
import com.elfmcys.ysm.platform.capability.NbtCapabilityCodec;

// PORT(26.3-fabric): the Forge ICapabilitySerializable is replaced by a persistent CapKey (Fabric data attachment).
// The class and field names are kept so existing entity.getCapability(...) call sites compile unchanged.
public final class ModelInfoCapabilityProvider {
    public static final CapKey<ModelInfoCapability> MODEL_INFO_CAP = CapKey.persistentKey("model_id",
        NbtCapabilityCodec.compound(ModelInfoCapability::new, ModelInfoCapability::serializeNBT,
            ModelInfoCapability::deserializeNBT),
        CapabilityRules::playerModelInfo, entity -> new ModelInfoCapability());

    private ModelInfoCapabilityProvider() {
    }
}
