package com.elfmcys.ysm.capability;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Forces the capability keys to register their Fabric attachment types during mod initialisation.
 * PORT(26.3-fabric): Forge registered capability types through RegisterCapabilitiesEvent / CapabilityToken.
 */
public final class CapabilityBootstrap {
    private CapabilityBootstrap() {
    }

    /** Both sides. Called from the main entrypoint. */
    public static void common() {
        ModelInfoCapabilityProvider.MODEL_INFO_CAP.id();
        AuthModelsCapabilityProvider.AUTH_MODELS_CAP.id();
        StarModelsCapabilityProvider.STAR_MODELS_CAP.id();
        ProjectileModelInfoCapabilityProvider.CAP.id();
        VehicleModelInfoCapabilityProvider.CAP.id();
    }

    /** Client-only keys. Called from the client entrypoint. */
    @Environment(EnvType.CLIENT)
    public static void client() {
        PlayerAnimatableCapabilityProvider.CAP.id();
        ClientLazyCapabilityProvider.CAP.id();
        VehicleAnimatableCapabilityProvider.CAP.id();
        ProjectileAnimatableCapabilityProvider.CAP.id();
    }
}
