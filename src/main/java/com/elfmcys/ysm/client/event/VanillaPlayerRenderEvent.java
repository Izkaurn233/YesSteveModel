package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.event.api.SpecialPlayerRenderEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.Identifier;
@SuppressWarnings("removal")
public class VanillaPlayerRenderEvent {
    private static final Identifier STEVE_SKIN_LOCATION =
        Identifier.parse("textures/entity/player/wide/steve.png");
    private static final Identifier ALEX_SKIN_LOCATION =
        Identifier.parse("textures/entity/player/slim/alex.png");
    private static final String STEVE = "misc/2_steve";
    private static final String ALEX = "misc/1_alex";

    public static void onRenderPlayer(SpecialPlayerRenderEvent event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        Player player = event.getPlayer();
        if (isVanillaPlayer(event.getModelId()) && player instanceof AbstractClientPlayer clientPlayer) {
            event.setTextureLocationOverride(clientPlayer.getSkin().body().texturePath());
        }
    }

    private static boolean isVanillaPlayer(String modelId) {
        return STEVE.equals(modelId) || ALEX.equals(modelId);
    }

    private static Identifier getDefaultSkin(String modelId) {
        return modelId.equals(STEVE) ? STEVE_SKIN_LOCATION : ALEX_SKIN_LOCATION;
    }
}
