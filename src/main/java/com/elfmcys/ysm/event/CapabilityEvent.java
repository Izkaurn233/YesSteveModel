package com.elfmcys.ysm.event;

import com.elfmcys.ysm.platform.Platform;
import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.AuthModelsCapability;
import com.elfmcys.ysm.capability.AuthModelsCapabilityProvider;
import com.elfmcys.ysm.capability.ModelInfoCapability;
import com.elfmcys.ysm.capability.ModelInfoCapabilityProvider;
import com.elfmcys.ysm.capability.ModelInfoSyncAssembler;
import com.elfmcys.ysm.capability.ProjectileModelInfoCapabilityProvider;
import com.elfmcys.ysm.capability.StarModelsCapability;
import com.elfmcys.ysm.capability.StarModelsCapabilityProvider;
import com.elfmcys.ysm.capability.VehicleModelInfoCapabilityProvider;
import com.elfmcys.ysm.platform.capability.CapOptional;
import com.elfmcys.ysm.config.ServerConfig;
import com.elfmcys.ysm.model.ModelRuntime;
import com.elfmcys.ysm.model.service.ServerModelService;
import com.elfmcys.ysm.network.NetworkHandler;
import com.elfmcys.ysm.network.forge.MinecraftStateHandler;
import com.elfmcys.ysm.network.forge.PlayerStateHandler;
import com.elfmcys.ysm.network.forge.SessionProtocolHandler;
import com.elfmcys.ysm.network.protocol.StarredModelSnapshots;
import com.elfmcys.ysm.proto.network.PlayerStateUpdateProto;

import java.util.Optional;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;

public final class CapabilityEvent {
    // PORT(26.3-fabric): the Forge AttachCapabilitiesEvent handler is gone; its attach rules live in
    // capability.CapabilityRules and are evaluated when a capability is first accessed.

    /** Registered on Fabric ServerPlayerEvents.COPY_FROM (Forge PlayerEvent.Clone). */
    public static void onPlayerCloned(ServerPlayer original, ServerPlayer clone) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        CapOptional<ModelInfoCapability> oldModelInfoCap = getModelInfoCap(original);
        CapOptional<AuthModelsCapability> oldAuthModelsCap = getAuthModelsCap(original);
        CapOptional<StarModelsCapability> oldStarModelsCap = getStarModelsCap(original);

        CapOptional<ModelInfoCapability> newModelInfoCap = getModelInfoCap(clone);
        CapOptional<AuthModelsCapability> newAuthModelsCap = getAuthModelsCap(clone);
        CapOptional<StarModelsCapability> newStarModelsCap = getStarModelsCap(clone);

        newModelInfoCap.ifPresent((newModelInfo) -> oldModelInfoCap.ifPresent(newModelInfo::moveFrom));
        newAuthModelsCap.ifPresent((newAuthModels) -> oldAuthModelsCap.ifPresent(newAuthModels::copyFrom));
        newStarModelsCap.ifPresent((newStarModels) -> oldStarModelsCap.ifPresent(newStarModels::copyFrom));
    }

    /** Registered on Fabric EntityTrackingEvents.START_TRACKING (Forge PlayerEvent.StartTracking). */
    public static void onTrackingPlayer(Entity target, ServerPlayer tracker) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (target instanceof ServerPlayer trackPlayer) {
            final Player player = tracker;
            getModelInfoCap(trackPlayer).ifPresent(cap -> {
                if (!NetworkHandler.isPlayerChannelPresent(trackPlayer) && !cap.isMandatory()) {
                    return;
                }
                buildModelInfoPacket(trackPlayer, cap).ifPresent(packet -> {
                    PlayerStateHandler.sendToObserver(packet, player);
                });
            });
        } else if (target instanceof Projectile projectile) {
            projectile.getCapability(ProjectileModelInfoCapabilityProvider.CAP).ifPresent(cap -> {
                if (cap.isInitialized()) {
                    NetworkHandler.sendToClientPlayer(MinecraftStateHandler.projectile(projectile.getId(), cap),
                        tracker);
                }
            });
        } else if (target != null) {
            target.getCapability(VehicleModelInfoCapabilityProvider.CAP).ifPresent(cap -> {
                if (cap.isInitialized()) {
                    NetworkHandler.sendToClientPlayer(MinecraftStateHandler.vehicle(target.getId(), cap),
                        tracker);
                }
            });
        }
    }

    /**
     * Registered on Fabric ServerEntityEvents.ENTITY_LOAD (Forge EntityJoinLevelEvent; only server players matter here).
     * TODO: not verified: whether ENTITY_LOAD fires for players at login and respawn exactly where Forge did.
     */
    public static void onEntityJoinWorld(Entity entity) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (entity instanceof ServerPlayer serverPlayer) {
            getModelInfoCap(serverPlayer).ifPresent(modelInfoCap -> {
                if (!NetworkHandler.isPlayerChannelPresent(serverPlayer) && !modelInfoCap.isMandatory()) {
                    modelInfoCap.consumeDirty();
                    return;
                }
                modelInfoCap.stopAnimation(serverPlayer);
                modelInfoCap.consumeDirty();
            });

            getAuthModelsCap(serverPlayer)
                .ifPresent(authModelsCap -> SessionProtocolHandler.refreshGrants(serverPlayer));

            getStarModelsCap(serverPlayer).ifPresent(starModelCap -> {
                NetworkHandler.sendToClientPlayer(StarredModelSnapshots.create(starModelCap.getStarModels()),
                    serverPlayer);
            });
        }
    }

    /**
     * 同步客户端服务端数据
     */
    public static void onServerTick(MinecraftServer server) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        {
            var players = server.getPlayerList().getPlayers();
            var lowBandwidthUsage = ServerConfig.LOW_BANDWIDTH_USAGE.get();
            for (ServerPlayer player : players) {
                getModelInfoCap(player).ifPresent(cap -> {
                    if (!NetworkHandler.isPlayerChannelPresent(player) && !cap.isMandatory()) {
                        cap.consumeDirty();
                        return;
                    }
                    if (cap.consumeDirty()) {
                        cap.getPropertiesTracker().tick(player, false, lowBandwidthUsage);
                        buildModelInfoPacket(player, cap).ifPresent(packet -> {
                            PlayerStateHandler.broadcast(player, packet);
                            if (player.getVehicle() != null && player.getVehicle().getFirstPassenger() == player) {
                                CapabilityEvent.onVehicleSetModel(player.getVehicle(), player);
                            }
                        });
                    } else {
                        cap.getPropertiesTracker().tick(player, true, lowBandwidthUsage);
                    }
                });
            }
            ServerModelService.current().ifPresent(service -> {
                if (Platform.isDedicatedServer()) {
                    var system = ModelRuntime.system();
                    system.tickCatalog();
                    system.tickServerRuntime();
                }
                service.tick();
            });
        }
    }

    public static void onProjectileSetOwner(Projectile projectile, ServerPlayer owner) {
        owner.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(ownerCap -> {
            if (!NetworkHandler.isPlayerChannelPresent(owner) && !ownerCap.isMandatory()) {
                return;
            }
            projectile.getCapability(ProjectileModelInfoCapabilityProvider.CAP).ifPresent(cap -> {
                ownerCap.executeWithMolangVars(molangVars -> {
                    cap.init(ownerCap.getModelId(), molangVars);
                    NetworkHandler.broadcastToVisiblePlayers(MinecraftStateHandler.projectile(projectile.getId(), cap),
                        projectile);
                });
            });
        });
    }

    public static void onVehicleSetModel(Entity vehicle, ServerPlayer owner) {
        owner.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(ownerCap -> {
            if (!NetworkHandler.isPlayerChannelPresent(owner) && !ownerCap.isMandatory()) {
                return;
            }
            vehicle.getCapability(VehicleModelInfoCapabilityProvider.CAP).ifPresent(cap -> {
                // 失败就丢弃
                ownerCap.getMolangVars().ifPresent(molangVars -> {
                    cap.update(ownerCap.getModelId(), molangVars);
                    NetworkHandler.broadcastToVisiblePlayers(MinecraftStateHandler.vehicle(vehicle.getId(), cap),
                        vehicle);
                });
            });
        });
    }

    private static CapOptional<ModelInfoCapability> getModelInfoCap(Player player) {
        return player.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP);
    }

    private static Optional<PlayerStateUpdateProto> buildModelInfoPacket(ServerPlayer player,
        ModelInfoCapability capability) {
        return ServerModelService.current().flatMap(ServerModelService::catalog)
            .flatMap(snapshot -> ModelInfoSyncAssembler.build(player, capability, snapshot));
    }

    private static CapOptional<AuthModelsCapability> getAuthModelsCap(Player player) {
        return player.getCapability(AuthModelsCapabilityProvider.AUTH_MODELS_CAP);
    }

    private static CapOptional<StarModelsCapability> getStarModelsCap(Player player) {
        return player.getCapability(StarModelsCapabilityProvider.STAR_MODELS_CAP);
    }
}
