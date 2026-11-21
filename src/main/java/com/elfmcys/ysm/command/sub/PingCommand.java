package com.elfmcys.ysm.command.sub;

import com.elfmcys.ysm.network.forge.SessionProtocolHandler;
import com.elfmcys.ysm.platform.Platform;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class PingCommand {
    private static final String MOLANG_NAME = "ping";

    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        LiteralArgumentBuilder<CommandSourceStack> ping = Commands.literal(MOLANG_NAME).executes(PingCommand::ping);
        return ping;
    }

    private static int ping(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        player.sendSystemMessage(Component.translatable("message.yes_steve_model.client.ping_result",
            Platform.ysmVersion()));
        SessionProtocolHandler.sendServerHello(player);
        return Command.SINGLE_SUCCESS;
    }
}
