package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.compat.backpack.sophisticated.SophisticatedCompat;
import com.elfmcys.ysm.client.renderer.CustomFirstPersonArmRenderer;
import com.elfmcys.ysm.client.renderer.CustomPlayerRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class RegisterEntityRenderersEvent {
    private static CustomPlayerRenderer CUSTOM_PLAYER_RENDERER;
    private static CustomFirstPersonArmRenderer CUSTOM_FIRST_PERSON_RENDERER;

    public static void onReload(EntityRendererProvider.Context context) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }

        var playerRenderer = new CustomPlayerRenderer(context);
        var armRenderer = new CustomFirstPersonArmRenderer();
        releaseFirstPersonArm();
        CUSTOM_PLAYER_RENDERER = playerRenderer;
        CUSTOM_FIRST_PERSON_RENDERER = armRenderer;

        SophisticatedCompat.addLayer();
    }

    public static CustomPlayerRenderer getPlayerRenderer() {
        return Objects.requireNonNull(CUSTOM_PLAYER_RENDERER, "Player renderer has not loaded");
    }

    public static CustomFirstPersonArmRenderer getFirstPersonArmRenderer() {
        return Objects.requireNonNull(CUSTOM_FIRST_PERSON_RENDERER, "First-person renderer has not loaded");
    }

    public static void releaseFirstPersonArm() {
        if (CUSTOM_FIRST_PERSON_RENDERER != null) {
            CUSTOM_FIRST_PERSON_RENDERER.release();
        }
    }

    public static void releaseFirstPersonArm(LocalPlayer player) {
        if (CUSTOM_FIRST_PERSON_RENDERER != null) {
            CUSTOM_FIRST_PERSON_RENDERER.releaseForPlayer(player);
        }
    }

}
