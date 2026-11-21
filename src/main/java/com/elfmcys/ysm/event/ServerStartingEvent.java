package com.elfmcys.ysm.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.model.service.ServerModelService;
import net.minecraft.server.MinecraftServer;

public class ServerStartingEvent {
    public static void onServerInit(MinecraftServer server) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        ServerModelService.start(server);
    }

    public static void onServerStopping(MinecraftServer server) {
        ServerModelService.current().ifPresent(ServerModelService::close);
    }
}
