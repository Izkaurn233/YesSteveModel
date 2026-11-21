package com.elfmcys.ysm.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.model.service.ServerModelService;
import net.minecraft.server.level.ServerPlayer;

public class LoggedOutEvent {
    public static void onPlayerLoggedOut(ServerPlayer player) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        ServerModelService.current().ifPresent(service -> service.closeSession(player));
    }
}
