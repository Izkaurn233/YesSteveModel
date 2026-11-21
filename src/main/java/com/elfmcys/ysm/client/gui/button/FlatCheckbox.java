package com.elfmcys.ysm.client.gui.button;

import com.elfmcys.ysm.YesSteveModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class FlatCheckbox extends AbstractWidget implements IConfigFormsButton {
    private static final Identifier BUTTON_TEXTURE =
        Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "texture/roulette.png");
    private final Consumer<Boolean> onClick;
    private boolean selected;

    public FlatCheckbox(int xIn, int yIn, int width, Component name, Consumer<Boolean> onClick) {
        super(xIn, yIn, width, 12, name);
        this.onClick = onClick;
    }

    public FlatCheckbox(int xIn, int yIn, Component name, Consumer<Boolean> onClick) {
        this(xIn, yIn, 115, name, onClick);
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, BUTTON_TEXTURE, getX(), getY(),
            selected ? 128 : 0, isHoveredOrFocused() ? 12 : 0, width, height, 256, 256);
        graphics.text(Minecraft.getInstance().font, getMessage(), getX() + 14, getY() + 2, 0xFFFFFFFF, false);
    }

    public void setStateTriggered(boolean selected) {
        this.selected = selected;
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        selected = !selected;
        onClick.accept(selected);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
