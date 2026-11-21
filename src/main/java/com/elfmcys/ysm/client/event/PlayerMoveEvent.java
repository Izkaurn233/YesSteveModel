package com.elfmcys.ysm.client.event;

import net.minecraft.client.input.KeyEvent;
import com.mojang.blaze3d.platform.InputConstants;
import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.network.NetworkHandler;
import com.elfmcys.ysm.network.forge.ClientProtocolGateway;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.world.phys.Vec2;
import net.minecraft.client.player.LocalPlayer;

import static com.elfmcys.ysm.client.input.AnimationRouletteKey.LOCK_ROULETTE_KEY;

public class PlayerMoveEvent {
    private static boolean LOCK_EXTRA_ANIMATION = false;

    public static void onKeyboardInput(KeyEvent event, int action) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (action == InputConstants.PRESS && LOCK_ROULETTE_KEY.matches(event)) {
            LOCK_EXTRA_ANIMATION = !LOCK_EXTRA_ANIMATION;
        }
    }

    /**
     * PORT(26.3-fabric): registered on Fabric ClientTickEvents.END_CLIENT_TICK (Forge TickEvent.Phase.END).
     * 改用 TickEvent.ClientTickEvent 监听按键状态，避免与其他模组（如 Touch Controller） 冲突时漏判按键。
     */
    public static void onClientTick() {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (LOCK_EXTRA_ANIMATION) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && isMoveKey(player)) {
            player.getCapability(PlayerAnimatableCapabilityProvider.CAP).ifPresent(cap -> {
                cap.waitForAsyncUpdate();
                if (cap.isPlayingExtraAnimation()) {
                    cap.stopExtraAnimation();
                    if (NetworkHandler.isRemoteChannelPresent()) {
                        ClientProtocolGateway.stopSelfAnimation();
                    }
                }
            });
        }
    }

    public static boolean isMoveKey(LocalPlayer player) {
        ClientInput input = player.input;
        if (input == null) {
            return false;
        }
        // PORT(26.3-fabric): leftImpulse/forwardImpulse are now the x/y of ClientInput.getMoveVector().
        Vec2 move = input.getMoveVector();
        return hasImpulse(move.x) || hasImpulse(move.y) || input.keyPresses.jump() || input.keyPresses.shift();
    }

    private static boolean hasImpulse(float impulse) {
        return Math.abs(impulse) > 1.0E-5F;
    }

    public static void switchLock() {
        LOCK_EXTRA_ANIMATION = !LOCK_EXTRA_ANIMATION;
    }

    public static boolean isLocked() {
        return LOCK_EXTRA_ANIMATION;
    }
}
