package com.elfmcys.ysm.client.input;

import com.elfmcys.ysm.platform.input.YsmKeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.client.event.PlayerMoveEvent;
import com.elfmcys.ysm.client.gui.AnimationRouletteScreen;
import com.elfmcys.ysm.network.forge.ClientProtocolGateway;
import com.elfmcys.ysm.util.InputCheckUtil;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.util.List;

import static com.elfmcys.ysm.client.gui.AnimationRouletteScreen.addRootClassify;

public class ExtraAnimationKey {
    public static final List<KeyMapping> EXTRA_ANIMATION_KEYS = Lists.newArrayList();

    public static void registerKeyBinding() {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        for (int i = 0; i <= 7; i++) {
            String name = String.format("key.yes_steve_model.extra_animation.%d.desc", i);
            KeyMapping keyMapping = new YsmKeyMapping(name, YsmKeyMapping.Modifier.NONE, InputConstants.UNKNOWN.getValue());
            KeyMappingHelper.registerKeyMapping(keyMapping);
            EXTRA_ANIMATION_KEYS.add(keyMapping);
        }
    }

    public static void onKeyboardInput(KeyEvent event, int action) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (!InputCheckUtil.isInGame()) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        for (KeyMapping key : EXTRA_ANIMATION_KEYS) {
            if (action == InputConstants.PRESS && InputCheckUtil.keyIsMatch(event, key) && player != null
                && !PlayerMoveEvent.isMoveKey(player)) {
                player.getCapability(PlayerAnimatableCapabilityProvider.CAP).ifPresent(cap -> {
                    var model = cap.getModelRenderTarget();
                    int index = EXTRA_ANIMATION_KEYS.indexOf(key);
                    var info = model.info();
                    var animations = info.getExtraAnimations();
                    if (animations.size() > index) {
                        String keyName = animations.get(index).key();
                        if ("#return".equals(keyName)) {
                            // #return 为停止播放轮盘动画
                            ClientProtocolGateway.stopSelfAnimation();
                        } else if (keyName.startsWith("#")
                            && info.getExtraAnimationClassifications().containsKey(keyName.substring(1))) {
                            addRootClassify(keyName.substring(1));
                            AnimationRouletteScreen screen = new AnimationRouletteScreen(
                                info.getExtraAnimationButtons(), info.getExtraAnimationClassifications(), model, cap);
                            Minecraft.getInstance().gui.setScreen(screen);
                        } else {
                            ClientProtocolGateway.playSelfAnimation(keyName);
                        }
                    }
                });
                return;
            }
        }
    }
}
