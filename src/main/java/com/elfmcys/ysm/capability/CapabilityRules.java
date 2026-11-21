package com.elfmcys.ysm.capability;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.platform.Platform;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;

/**
 * The attach rules of the Forge {@code CapabilityEvent.onAttachCapabilityEvent}, evaluated on access.
 * PORT(26.3-fabric): keep these in step with that original method (see {@code git show dev/1.20:...CapabilityEvent.java}).
 */
final class CapabilityRules {
    private CapabilityRules() {
    }

    private static boolean serverSide(Entity entity) {
        return YesSteveModel.isAvailable() && !entity.level().isClientSide();
    }

    private static boolean clientSide(Entity entity) {
        return YesSteveModel.isAvailable() && Platform.isClient() && entity.level().isClientSide();
    }

    /** Players, server side. */
    static boolean playerModelInfo(Entity entity) {
        return entity instanceof Player && serverSide(entity);
    }

    /** Players, both sides. */
    static boolean playerModels(Entity entity) {
        return entity instanceof Player && YesSteveModel.isAvailable();
    }

    /** Projectiles, server side. */
    static boolean projectileModelInfo(Entity entity) {
        return entity instanceof Projectile && serverSide(entity);
    }

    /** Every other entity (neither player nor projectile), server side. */
    static boolean vehicleModelInfo(Entity entity) {
        return !(entity instanceof Player) && !(entity instanceof Projectile) && serverSide(entity);
    }

    /** Client players. Kept free of client classes so this class loads on a dedicated server. */
    static boolean clientPlayerAnimatable(Entity entity) {
        return entity instanceof Player && clientSide(entity);
    }

    /** Client entities that are not players get the lazy vehicle/projectile holder. */
    static boolean clientLazy(Entity entity) {
        return !(entity instanceof Player) && clientSide(entity);
    }
}
