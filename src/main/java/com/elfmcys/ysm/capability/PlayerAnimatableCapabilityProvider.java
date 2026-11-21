package com.elfmcys.ysm.capability;

import com.elfmcys.ysm.platform.capability.CapKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;

// PORT(26.3-fabric): the Forge ICapabilityProvider is replaced by a transient CapKey (Fabric data attachment).
@Environment(EnvType.CLIENT)
public final class PlayerAnimatableCapabilityProvider {
    public static final CapKey<PlayerAnimatableCapability> CAP = CapKey.transientKey("animatable",
        CapabilityRules::clientPlayerAnimatable, entity -> new PlayerAnimatableCapability((Player)entity));

    private PlayerAnimatableCapabilityProvider() {
    }
}
