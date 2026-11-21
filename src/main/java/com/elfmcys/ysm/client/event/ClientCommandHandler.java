package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.command.ClientRootCommand;
import com.elfmcys.ysm.client.command.ResourceDebugCommand;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

@Environment(EnvType.CLIENT)
public final class ClientCommandHandler {
    private ClientCommandHandler() {}

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        ResourceDebugCommand.register(dispatcher);
        if (YesSteveModel.isAvailable()) {
            ClientRootCommand.register(dispatcher);
        }
    }
}
