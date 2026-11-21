package com.elfmcys.ysm.capability;

import com.elfmcys.ysm.platform.capability.CapKey;
import com.elfmcys.ysm.platform.capability.NbtCapabilityCodec;

// PORT(26.3-fabric): the Forge ICapabilitySerializable is replaced by a persistent CapKey (Fabric data attachment).
public final class ProjectileModelInfoCapabilityProvider {
    public static final CapKey<ProjectileModelInfoCapability> CAP = CapKey.persistentKey("projectile_model_id",
        NbtCapabilityCodec.compound(ProjectileModelInfoCapability::new, ProjectileModelInfoCapability::serializeNBT,
            ProjectileModelInfoCapability::deserializeNBT),
        CapabilityRules::projectileModelInfo, entity -> new ProjectileModelInfoCapability());

    private ProjectileModelInfoCapabilityProvider() {
    }
}
