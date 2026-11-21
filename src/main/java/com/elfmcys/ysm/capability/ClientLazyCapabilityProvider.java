package com.elfmcys.ysm.capability;

import com.elfmcys.ysm.platform.capability.CapKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.projectile.Projectile;

// PORT(26.3-fabric): the Forge ICapabilityProvider is replaced by a transient CapKey (Fabric data attachment).
@Environment(EnvType.CLIENT)
public final class ClientLazyCapabilityProvider {
    public static final CapKey<ClientLazyCapability> CAP = CapKey.transientKey("client_lazy",
        CapabilityRules::clientLazy,
        entity -> new ClientLazyCapability(new VehicleAnimatableCapabilityProvider(entity),
            entity instanceof Projectile projectile ? new ProjectileAnimatableCapabilityProvider(projectile) : null));

    private ClientLazyCapabilityProvider() {
    }
}
