package com.elfmcys.ysm.client.gui.button;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.entity.AnimatableEntity;
import com.elfmcys.ysm.info.roulette.forms.ConfigActionRoute;
import com.elfmcys.ysm.network.forge.ClientProtocolGateway;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.text.DecimalFormat;

@SuppressWarnings("removal")
public class FlatSlider extends RangeSlider implements IConfigFormsButton {
    private static final Identifier BUTTON_TEXTURE =
        Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "texture/roulette.png");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
    private final AnimatableEntity<?> animatableEntity;
    private final long expressionGeneration;
    private final ConfigActionRoute route;

    public FlatSlider(int x, int y, Component prefix, double currentValue, AnimatableEntity<?> animatableEntity,
        long expressionGeneration, ConfigActionRoute route, double step, double min, double max) {
        super(x, y, 115, 15, prefix, Component.empty(), min, max, currentValue, step);
        this.animatableEntity = animatableEntity;
        this.expressionGeneration = expressionGeneration;
        this.route = route;
    }

    @Override
    protected void applyValue() {
        var value = (float)getValue();
        try {
            ClientProtocolGateway.submitConfigFloat(animatableEntity, expressionGeneration, route, value);
        } catch (RuntimeException exception) {
            YesSteveModel.LOGGER.error(exception);
        }
    }

    @Override
    public String getValueString() {
        return DECIMAL_FORMAT.format(this.getValue());
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int trackY = isFocused() && !canChangeValue ? 44 : 24;
        int handleY = isHovered || canChangeValue ? 84 : 64;
        drawSliderBar(graphics, getX(), width, trackY);
        drawSliderBar(graphics, getX() + (int)(value * (width - HANDLE_WIDTH)), HANDLE_WIDTH, handleY);
        extractScrollingStringOverContents(graphics.textRenderer(), getMessage(), TEXT_MARGIN);
        handleCursor(graphics);
    }

    private void drawSliderBar(GuiGraphicsExtractor graphics, int x, int width, int textureY) {
        int color = (int)Math.ceil(alpha * 255.0F) << 24 | 0xFFFFFF;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BUTTON_TEXTURE, x, getY(),
            0, textureY, 2, height, 256, 256, color);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BUTTON_TEXTURE, x + 2, getY(),
            2, textureY, width - 5, height, 195, 15, 256, 256, color);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BUTTON_TEXTURE, x + width - 3, getY(),
            197, textureY, 3, height, 256, 256, color);
    }
}
