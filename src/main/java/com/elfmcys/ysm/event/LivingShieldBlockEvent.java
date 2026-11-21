package com.elfmcys.ysm.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.ModelInfoCapabilityProvider;
import net.minecraft.server.level.ServerPlayer;

public class LivingShieldBlockEvent {
    public static void onShieldBlock(ServerPlayer player) {
        if (YesSteveModel.isAvailable()) {
            player.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(cap ->
                cap.getPropertiesTracker().recordShieldBlock(player));
        }
    }
}
