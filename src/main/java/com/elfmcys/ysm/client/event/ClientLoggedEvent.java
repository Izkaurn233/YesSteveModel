package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.network.forge.ClientSessionRuntime;
import net.minecraft.network.Connection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ClientLoggedEvent {
    private static Connection loggedIn;

    public static void onPlayerLoggedIn(Connection connection) {
        if (loggedIn == connection) {
            return;
        }
        if (!YesSteveModel.isAvailable()) {
            loggedIn = connection;
            YesSteveModel.sendUnavailableMessage();
            return;
        }
        ClientSessionRuntime.beginConnection(connection);
        loggedIn = connection;
    }

    public static void onPlayerLoggedOut(Connection connection) {
        if (loggedIn != connection) {
            return;
        }
        loggedIn = null;
        RegisterEntityRenderersEvent.releaseFirstPersonArm();
        if (YesSteveModel.isAvailable()) {
            ClientSessionRuntime.disconnect(connection);
        }
    }
}
