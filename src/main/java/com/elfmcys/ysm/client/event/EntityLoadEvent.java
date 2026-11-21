package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.animation.AnimationParallelTicker;
import com.elfmcys.ysm.client.gui.ModelScreen;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.capability.ProjectileAnimatableCapabilityProvider;
import com.elfmcys.ysm.capability.VehicleAnimatableCapabilityProvider;
import com.elfmcys.ysm.util.AnimatableCacheUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class EntityLoadEvent {
    private static final Cache<Integer, List<Consumer<Entity>>> CACHE =
        CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.SECONDS).build();

    public static void onEntityLoadToWorld(Entity entity) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        var list = CACHE.getIfPresent(entity.getId());
        if (list != null) {
            for (var consumer : list) {
                consumer.accept(entity);
            }
        }
        CACHE.invalidate(entity.getId());
    }

    public static void onEntityLeaveWorld(Entity entity) {
        if (entity instanceof LocalPlayer player) {
            RegisterEntityRenderersEvent.releaseFirstPersonArm(player);
        }
        if (entity instanceof Player) {
            entity.getCapability(PlayerAnimatableCapabilityProvider.CAP).ifPresent(capability -> capability.retire());
        }
        entity.getCapability(ProjectileAnimatableCapabilityProvider.CAP).ifPresent(capability -> capability.retire());
        entity.getCapability(VehicleAnimatableCapabilityProvider.CAP).ifPresent(capability -> capability.retire());
        CACHE.invalidate(entity.getId());
    }

    public static void onClientLevelUnload(ClientLevel level) {
        ModelScreen.unload(level);
        var player = Minecraft.getInstance().player;
        if (player != null && player.level() == level)
            RegisterEntityRenderersEvent.releaseFirstPersonArm(player);
        // Retire existing owners without resolving lazy capabilities for otherwise unused entities.
        AnimationParallelTicker.retireLevel(level);
        CACHE.invalidateAll();
        AnimatableCacheUtil.ENTITIES_CACHE.invalidateAll();
    }

    public static void executeOnEntity(int entityId, Consumer<Entity> consumer) {
        Minecraft.getInstance().execute(() -> {
            var level = Minecraft.getInstance().level;
            if (level != null) {
                var entity = level.getEntity(entityId);
                if (entity != null) {
                    consumer.accept(entity);
                } else {
                    addRecoveryHandler(entityId, consumer);
                }
            }
        });
    }

    // 非线程安全
    private static void addRecoveryHandler(int entityId, Consumer<Entity> consumer) {
        var list = CACHE.getIfPresent(entityId);
        if (list == null) {
            list = new ArrayList<>(3);
            CACHE.put(entityId, list);
        }
        list.add(consumer);
    }
}
