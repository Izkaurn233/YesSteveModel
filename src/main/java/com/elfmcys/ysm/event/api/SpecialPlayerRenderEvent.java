package com.elfmcys.ysm.event.api;

import com.elfmcys.ysm.api.event.CancellableEvent;
import com.elfmcys.ysm.client.entity.CustomPlayerEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class SpecialPlayerRenderEvent extends CancellableEvent {
    private final Player player;
    private final CustomPlayerEntity customPlayer;
    private final String modelId;
    @Nullable
    private ResourceLocation textureLocationOverride;

    // Forge required a no-arg constructor; kept for source compatibility.
    public SpecialPlayerRenderEvent() {
        player = null;
        customPlayer = null;
        modelId = null;
    }

    public SpecialPlayerRenderEvent(Player player, CustomPlayerEntity customPlayer, String modelId) {
        this.player = player;
        this.customPlayer = customPlayer;
        this.modelId = modelId;
    }

    public Player getPlayer() {
        return player;
    }

    public CustomPlayerEntity getCustomPlayer() {
        return customPlayer;
    }

    public String getModelId() {
        return modelId;
    }

    @Nullable
    public ResourceLocation getTextureLocationOverride() {
        return textureLocationOverride;
    }

    public void setTextureLocationOverride(@Nullable ResourceLocation textureLocationOverride) {
        this.textureLocationOverride = textureLocationOverride;
    }
}
