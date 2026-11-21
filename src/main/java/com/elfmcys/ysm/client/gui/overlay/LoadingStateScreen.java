package com.elfmcys.ysm.client.gui.overlay;

import com.elfmcys.ysm.client.gui.CatalogProgressText;
import com.elfmcys.ysm.config.LoadingStateScreenConfig;
import com.elfmcys.ysm.model.catalog.CatalogScanProgress;
import com.elfmcys.ysm.model.service.ClientModelService;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class LoadingStateScreen implements HudElement {
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (LoadingStateScreenConfig.DISABLE_LOADING_STATE_SCREEN.get())
            return;
        var service = ClientModelService.current().orElse(null);
        if (service == null)
            return;
        var progress = service.catalogProgress();
        if (progress == null)
            return;
        var color = switch (progress.phase()) {
            case DISCOVERING -> ChatFormatting.LIGHT_PURPLE;
            case PROCESSING -> ChatFormatting.GREEN;
            case FINALIZING -> ChatFormatting.GOLD;
        };
        var text = Component.translatable("gui.yes_steve_model.sync_hint.title")
            .append(CatalogProgressText.of(progress).copy().withStyle(color));
        var position = LoadingStateScreenConfig.LOADING_STATE_POSITION.get();
        var font = Minecraft.getInstance().font;
        var textWidth = font.width(text);
        var screenWidth = graphics.guiWidth();
        var screenHeight = graphics.guiHeight();
        var x = switch (position) {
            case TOP_LEFT, BOTTOM_LEFT -> 10;
            case TOP_CENTER, BOTTOM_CENTER -> (screenWidth - textWidth) / 2;
            case TOP_RIGHT, BOTTOM_RIGHT -> screenWidth - 10 - textWidth;
        };
        var y = switch (position) {
            case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> 10;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> screenHeight - 30;
            case BOTTOM_CENTER -> screenHeight - 85;
        };
        graphics.text(font, text, x, y, 0xFFFFFFFF);
        if (progress.phase() == CatalogScanProgress.Phase.PROCESSING && progress.total() > 0) {
            var barWidth = 150;
            var barHeight = 10;
            var barX = switch (position) {
                case TOP_LEFT, BOTTOM_LEFT -> 10;
                case TOP_CENTER, BOTTOM_CENTER -> (screenWidth - barWidth) / 2;
                case TOP_RIGHT, BOTTOM_RIGHT -> screenWidth - 10 - barWidth;
            };
            var barY = y + 12;
            graphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF555555);
            graphics.fill(barX, barY, barX + (int)(barWidth * ((float)progress.processed() / progress.total())),
                barY + barHeight, 0xFF00FF00);
        }
    }
}
