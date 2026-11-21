package com.elfmcys.ysm.util;

import com.elfmcys.ysm.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permission;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.network.chat.Component;

public class CommandUtil {
    private static final String MESSAGE_HEADER = "§6§l【§aYSM§6§l】§r";

    public static Component wrapMessage(Component message) {
        return Component.literal(MESSAGE_HEADER).append(message);
    }

    // 是否为单人模式，或联机模式下的房主
    public static boolean isLocalPlayer(Entity player) {
        return player != null && Platform.isClient()
            && player.getUUID().equals(Minecraft.getInstance().getUser().getProfileId());
    }

    public static boolean hasPermission(CommandSourceStack source, Permission permission) {
        return source.permissions().hasPermission(permission) || isLocalPlayer(source.getEntity());
    }

    public static void sendAsyncFeedback(final CommandSourceStack oldSource, final Component feedbackMsg,
        final boolean allowLogging) {
        final MinecraftServer server = oldSource.getServer();
        if (server.isStopped()) {
            return;
        }
        server.execute(() -> {
            CommandSourceStack commandSource = null;
            if (oldSource.getEntity() instanceof ServerPlayer player) {
                ServerPlayer newSender = server.getPlayerList().getPlayer(player.getUUID());
                if (newSender != null) {
                    commandSource = newSender.createCommandSourceStack();
                }
            }
            if (commandSource == null) {
                commandSource = server.createCommandSourceStack();
            }
            commandSource.sendSuccess(() -> feedbackMsg, allowLogging);
        });
    }
}
