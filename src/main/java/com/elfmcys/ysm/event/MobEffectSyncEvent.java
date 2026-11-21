package com.elfmcys.ysm.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.ModelInfoCapabilityProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Collection;

@SuppressWarnings("resource")
public class MobEffectSyncEvent {
    public static void onAddedOrUpdated(ServerPlayer player, MobEffectInstance effect) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        player.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(cap ->
            cap.getPropertiesTracker().addEffect(player, effect.getEffect().value(), effect.getAmplifier() + 1));
    }

    public static void onRemoved(ServerPlayer player, Collection<MobEffectInstance> effects) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        player.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(cap -> {
            for (var effect : effects) {
                cap.getPropertiesTracker().removeEffect(player, effect.getEffect().value());
            }
        });
    }
}
