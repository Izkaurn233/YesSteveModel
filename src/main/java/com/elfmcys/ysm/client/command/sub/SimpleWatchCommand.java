package com.elfmcys.ysm.client.command.sub;

import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.client.gui.overlay.DebugAnimationScreen;
import com.elfmcys.ysm.geckolib3.core.processor.DebugInfo;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

import static com.elfmcys.ysm.client.command.ClientRootCommand.ALL_CONTROLLERS;
import static com.elfmcys.ysm.client.command.ClientRootCommand.ALL_VARS;

public class SimpleWatchCommand {
    private static final String WATCH_NAME = "watch";

    private static final String VAR_NAME = "var";
    private static final String STATE_NAME = "state";
    private static final String CLEAR_NAME = "clear";

    private static final String EXPRESSION_NAME = "exp";
    private static final String CONTROLLER_NAME = "controller";

    /**
     * 简化版本 ysmclient watch var <exp> 等效于 ysmclient molang watch add post <exp_name> <exp>
     */
    public static LiteralArgumentBuilder<FabricClientCommandSource> get() {
        LiteralArgumentBuilder<FabricClientCommandSource> watch = ClientCommands.literal(WATCH_NAME);

        LiteralArgumentBuilder<FabricClientCommandSource> var = ClientCommands.literal(VAR_NAME);
        LiteralArgumentBuilder<FabricClientCommandSource> state = ClientCommands.literal(STATE_NAME);
        LiteralArgumentBuilder<FabricClientCommandSource> clear = ClientCommands.literal(CLEAR_NAME);

        Supplier<RequiredArgumentBuilder<FabricClientCommandSource, String>> exp =
            () -> ClientCommands.argument(EXPRESSION_NAME, StringArgumentType.greedyString()).suggests(ALL_VARS);
        Supplier<RequiredArgumentBuilder<FabricClientCommandSource, String>> controller =
            () -> ClientCommands.argument(CONTROLLER_NAME, StringArgumentType.greedyString()).suggests(ALL_CONTROLLERS);

        watch.then(var.then(exp.get().executes(SimpleWatchCommand::addExpression)));
        watch.then(state.then(controller.get().executes(SimpleWatchCommand::addControllerState)));
        watch.then(clear.executes(SimpleWatchCommand::clearExpression));

        return watch;
    }

    @SuppressWarnings("all")
    private static int addExpression(CommandContext<FabricClientCommandSource> ctx) {
        if (!isClientReady()) {
            return Command.SINGLE_SUCCESS;
        }
        Minecraft mc = Minecraft.getInstance();
        String exp = StringArgumentType.getString(ctx, EXPRESSION_NAME);
        mc.execute(() -> mc.player.getCapability(PlayerAnimatableCapabilityProvider.CAP).ifPresent(cap -> {
            DebugAnimationScreen.getDebugInfo().add(DebugInfo.Phase.POST_ANIMATION, exp, exp);
            // 强制打开调试界面
            if (!DebugAnimationScreen.isEnabled()) {
                DebugAnimationScreen.enableForLocalPlayer();
            }
        }));

        return Command.SINGLE_SUCCESS;
    }

    @SuppressWarnings("all")
    private static int clearExpression(CommandContext<FabricClientCommandSource> ctx) {
        if (!isClientReady()) {
            return Command.SINGLE_SUCCESS;
        }
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> mc.player.getCapability(PlayerAnimatableCapabilityProvider.CAP)
            .ifPresent(cap -> DebugAnimationScreen.getDebugInfo().clear()));
        DebugAnimationScreen.clearDebugController();
        return Command.SINGLE_SUCCESS;
    }

    @SuppressWarnings("all")
    private static int addControllerState(CommandContext<FabricClientCommandSource> ctx) {
        if (!isClientReady()) {
            return Command.SINGLE_SUCCESS;
        }
        Minecraft mc = Minecraft.getInstance();
        String controllerName = StringArgumentType.getString(ctx, CONTROLLER_NAME);
        // 强制打开调试界面
        DebugAnimationScreen.addDebugController(controllerName);
        if (!DebugAnimationScreen.isEnabled()) {
            DebugAnimationScreen.enableForLocalPlayer();
        }

        return Command.SINGLE_SUCCESS;
    }

    @SuppressWarnings("ConstantValue")
    private static boolean isClientReady() {
        return Minecraft.getInstance() != null && Minecraft.getInstance().player != null;
    }
}
