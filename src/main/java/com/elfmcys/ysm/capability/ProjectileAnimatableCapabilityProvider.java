package com.elfmcys.ysm.capability;

import com.elfmcys.ysm.platform.capability.CapKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.projectile.Projectile;
import org.jetbrains.annotations.Nullable;

/**
 * Holder of a client projectile animatable, initialised on demand by the network layer.
 * PORT(26.3-fabric): {@link #CAP} is a derived key, present only once {@link #initialize()} has run, as in Forge.
 */
@Environment(EnvType.CLIENT)
public class ProjectileAnimatableCapabilityProvider {
    public static final CapKey<ProjectileAnimatableCapability> CAP = CapKey.derivedKey("projectile_animatable",
        entity -> {
            var lazy = ClientLazyCapabilityProvider.CAP.peek(entity);
            var provider = lazy == null ? null : lazy.getProjectileAnimatableCapabilityProvider();
            return provider == null ? null : provider.instance();
        });

    private ProjectileAnimatableCapability instance;
    private Projectile projectile;

    public ProjectileAnimatableCapabilityProvider(Projectile projectile) {
        this.projectile = projectile;
    }

    public ProjectileAnimatableCapability initialize() {
        if (this.instance == null) {
            this.instance = new ProjectileAnimatableCapability(projectile);
            this.projectile = null;
        }
        return this.instance;
    }

    public @Nullable ProjectileAnimatableCapability instance() {
        return instance;
    }
}
