package com.elfmcys.ysm.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.network.forge.SessionProtocolHandler;
import net.minecraft.server.level.ServerPlayer;

public final class LoginEvent {
    public static void onLoggedInServer(ServerPlayer player) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        SessionProtocolHandler.sendServerHello(player);
    }
}
