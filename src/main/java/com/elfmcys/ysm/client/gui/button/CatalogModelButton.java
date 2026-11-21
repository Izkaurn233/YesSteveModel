package com.elfmcys.ysm.client.gui.button;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.StarModelsCapabilityProvider;
import com.elfmcys.ysm.client.gui.GuiModelPreview;
import com.elfmcys.ysm.client.gui.CustomGuiPlayerEntity;
import com.elfmcys.ysm.client.demand.ClientDemandCoordinator;
import com.elfmcys.ysm.model.resource.client.ModelRenderTarget;
import com.elfmcys.ysm.model.catalog.client.entry.CatalogModelMetadata;
import com.elfmcys.ysm.model.catalog.client.entry.ClientCatalogEntry;
import com.elfmcys.ysm.client.texture.TextureHolder;
import com.elfmcys.ysm.config.ClientConfig;
import com.elfmcys.ysm.model.domain.Hash256;
import com.elfmcys.ysm.util.ModelIdUtil;
import com.elfmcys.ysm.platform.input.GlfwKeyCompat;
import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Keeps images independent from the card's temporary live presentation. */
public final class CatalogModelButton extends Button implements AutoCloseable {
    private static final Identifier ICON = Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "texture/icon.png");

    private final ClientCatalogEntry entry;
    private final CatalogModelMetadata metadata;
    private final CustomGuiPlayerEntity entity;
    private final java.util.function.BooleanSupplier authorizationRequired;
    private final SelectionHandler selection;
    private final RenderTargetHandler openRenderTarget;
    private final Component pathName;
    private final CatalogModelCardState state;
    private final java.util.function.BooleanSupplier navigation;

    public CatalogModelButton(int x, int y, ClientCatalogEntry entry,
        java.util.function.BooleanSupplier authorizationRequired, CatalogPageFrame frame,
        ClientDemandCoordinator.PageScope page, CustomGuiPlayerEntity entity, SelectionHandler selection,
        RenderTargetHandler openRenderTarget, java.util.function.BooleanSupplier navigation) {
        super(x, y, 52, 90, name(CatalogModelMetadata.from(entry)), ignored -> {
        }, DEFAULT_NARRATION);
        this.entry = entry;
        this.metadata = CatalogModelMetadata.from(entry);
        this.entity = entity;
        this.authorizationRequired = authorizationRequired;
        this.selection = selection;
        this.openRenderTarget = openRenderTarget;
        this.navigation = navigation;
        this.pathName = Component.literal(ModelIdUtil.getFileNameFromPath(metadata.path()));
        this.state = new CatalogModelCardState(frame, page, entry, metadata, entity);
    }

    public Hash256 modelHash() {
        return entry.modelHash();
    }

    public @Nullable ModelRenderTarget renderTarget() {
        return state.renderTarget();
    }

    public ClientDemandCoordinator.Target target() {
        return state.target();
    }

    @Override
    public Component getMessage() {
        return ClientConfig.SHOW_MODEL_ID_FIRST.get() ? pathName : super.getMessage();
    }

    @Override
    public void onPress(InputWithModifiers input) {
        if (!authorizationRequired.getAsBoolean()) {
            selection.select(entry.modelHash(), metadata.path(), metadata.defaultTexture(), state.renderTarget());
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (isMouseOver(mouseX, mouseY) && event.hasControlDownWithQuirk()
            && CatalogCardActions.click(true, GlfwKeyCompat.toGlfwMouseButton(button), metadata.path(), entry,
                Minecraft.getInstance().isLocalServer(),
                value -> Minecraft.getInstance().keyboardHandler.setClipboard(value),
                CatalogModelButton::openDirectory))
            return true;
        if (button == InputConstants.MOUSE_BUTTON_RIGHT && isMouseOver(mouseX, mouseY) && !authorizationRequired.getAsBoolean()
            && navigation.getAsBoolean()) {
            openRenderTarget.open(target());
            return true;
        }
        return !authorizationRequired.getAsBoolean() && super.mouseClicked(event, doubleClick);
    }

    private static void openDirectory(java.nio.file.Path directory) {
        try {
            if (!java.nio.file.Files.readAttributes(directory, java.nio.file.attribute.BasicFileAttributes.class)
                .isDirectory())
                throw new java.io.IOException("Model entry directory is unavailable");
            Blaze3D.openPath(directory);
        } catch (java.io.IOException | RuntimeException failure) {
            YesSteveModel.LOGGER.error("Failed to open model entry directory", failure);
            var player = Minecraft.getInstance().player;
            if (player != null)
                player.sendSystemMessage(Component.literal("Unable to open the model directory"));
        }
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        var color = authorizationRequired.getAsBoolean() ? 0x7F000000 : 0xFF434242;
        graphics.fillGradient(getX(), getY(), getX() + width, getY() + height, color, color);
        var renderTarget = state.renderTarget();
        if (renderTarget != null) {
            renderImage(graphics, state.background());
            renderEntity(graphics);
            renderImage(graphics, state.foreground());
        } else {
            var preview = state.preview();
            if (preview != null) {
                renderImage(graphics, preview);
            } else if (state.loading()) {
                renderLoading(graphics);
            } else {
                graphics.centeredText(Minecraft.getInstance().font, Component.literal("?"), getX() + width / 2,
                    getY() + (height - 20) / 2, 0xFFAAAAAA);
            }
        }
        renderName(graphics);
        renderState(graphics);
    }

    public void renderTooltip(GuiGraphicsExtractor graphics, Screen screen, int mouseX, int mouseY) {
        if (!isHovered()) {
            return;
        }
        var locale = Minecraft.getInstance().getLanguageManager().getSelected();
        var input = CatalogModelTooltipFormatter.input(metadata, entry, locale, state.loadError());
        var player = Minecraft.getInstance().player;
        boolean shiftDown = InputConstants.isKeyDown(InputConstants.KEY_LSHIFT)
            || InputConstants.isKeyDown(InputConstants.KEY_RSHIFT);
        var lines = CatalogModelTooltipFormatter.format(input, shiftDown, Component::translatable,
            state.missingPreview(), player != null && player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER));
        graphics.setComponentTooltipForNextFrame(Minecraft.getInstance().font, lines, mouseX, mouseY);
    }

    @Override
    public void close() {
        state.close();
    }

    private void renderEntity(GuiGraphicsExtractor graphics) {
        GuiModelPreview.prepared(graphics, getX(), getY(), width, height - 20, getY() + height / 2.0F + 20.0F,
            30.0F, entity, state.frameInput(), state.prepared());
    }

    CatalogModelCardState prepareFrame(float partialTick, int mouseX, int mouseY, long now) {
        state.captureInput(partialTick, isMouseOver(mouseX, mouseY), isFocused(), now,
            metadata.info().getSettings().disablePreviewRotation());
        return state;
    }

    private void renderImage(GuiGraphicsExtractor graphics, @Nullable TextureHolder image) {
        if (image == null) {
            return;
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, image.id(), getX(), getY(), 0, 0, width, height, width, height);
    }

    private void renderLoading(GuiGraphicsExtractor graphics) {
        var phase = (int)((Util.getMillis() / 250L) % 4L);
        var dots = ".".repeat(phase);
        graphics.centeredText(Minecraft.getInstance().font, Component.literal(dots), getX() + width / 2, getY() + (height - 20) / 2,
            0xFFF3EFE0);
    }

    private void renderName(GuiGraphicsExtractor graphics) {
        Font font = Minecraft.getInstance().font;
        List<FormattedCharSequence> split = font.split(getMessage(), 45);
        if (split.size() > 1) {
            graphics.centeredText(font, split.get(0), getX() + width / 2, getY() + height - 19, 0xFFF3EFE0);
            graphics.centeredText(font, split.get(1), getX() + width / 2, getY() + height - 10, 0xFFF3EFE0);
        } else {
            graphics.centeredText(font, getMessage(), getX() + width / 2, getY() + height - 15, 0xFFF3EFE0);
        }
    }

    private void renderState(GuiGraphicsExtractor graphics) {
        if (!authorizationRequired.getAsBoolean() && isHoveredOrFocused() && navigation.getAsBoolean()) {
            graphics.fillGradient(getX(), getY() + 1, getX() + 1, getY() + height - 1, 0xFFF3EFE0, 0xFFF3EFE0);
            graphics.fillGradient(getX(), getY(), getX() + width, getY() + 1, 0xFFF3EFE0, 0xFFF3EFE0);
            graphics.fillGradient(getX() + width - 1, getY() + 1, getX() + width, getY() + height - 1, 0xFFF3EFE0,
                0xFFF3EFE0);
            graphics.fillGradient(getX(), getY() + height - 1, getX() + width, getY() + height, 0xFFF3EFE0,
                0xFFF3EFE0);
        }
        if (authorizationRequired.getAsBoolean()) {
            graphics.fillGradient(getX(), getY(), getX() + width, getY() + height, 0x9F222222, 0x9F222222);
        }
        var player = Minecraft.getInstance().player;
        if (player != null) {
            player.getCapability(StarModelsCapabilityProvider.STAR_MODELS_CAP).ifPresent(stars -> {
                if (stars.containModel(entry.modelHash())) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, ICON, getX() + width - 14, getY(), 16, 0, 16, 16, 256, 256);
                }
            });
        }
    }

    private static Component name(CatalogModelMetadata metadata) {
        return Component.literal(CatalogModelTooltipFormatter.displayName(metadata,
            Minecraft.getInstance().getLanguageManager().getSelected()));
    }

    @FunctionalInterface
    public interface SelectionHandler {
        void select(Hash256 hash, String path, String texture, @Nullable ModelRenderTarget renderTarget);
    }

    @FunctionalInterface
    public interface RenderTargetHandler {
        void open(ClientDemandCoordinator.Target target);
    }
}
