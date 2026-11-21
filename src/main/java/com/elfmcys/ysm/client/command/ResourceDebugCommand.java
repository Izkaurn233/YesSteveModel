package com.elfmcys.ysm.client.command;

import com.elfmcys.ysm.util.ResourceDiagnostics;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public final class ResourceDebugCommand {
    private ResourceDebugCommand() {}

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        var dump = ClientCommands.literal("dump");
        for (var kind : ResourceDiagnostics.Kind.values()) {
            dump.then(ClientCommands.literal(kind.name().toLowerCase(Locale.ROOT)).executes(context -> {
                var count = ResourceDiagnostics.dump(kind);
                context.getSource().sendFeedback(Component.literal("YSM resource dump "
                    + kind.name().toLowerCase(Locale.ROOT) + ": " + count + " live resources; see log"));
                return Command.SINGLE_SUCCESS;
            }));
        }
        dispatcher.register(ClientCommands.literal("ysmdebug").then(ClientCommands.literal("resource")
            .then(ClientCommands.argument("enabled", BoolArgumentType.bool()).executes(context -> {
                var enabled = BoolArgumentType.getBool(context, "enabled");
                ResourceDiagnostics.setEnabled(enabled);
                context.getSource().sendFeedback(Component.literal("YSM resource logging: " + enabled));
                return Command.SINGLE_SUCCESS;
            })).then(dump)));
    }
}
