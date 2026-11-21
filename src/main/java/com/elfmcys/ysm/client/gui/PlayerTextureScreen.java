package com.elfmcys.ysm.client.gui;

import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.client.animation.AnimationRegister;
import com.elfmcys.ysm.client.gui.button.CatalogTextureButton;
import com.elfmcys.ysm.client.gui.button.FlatColorButton;
import com.elfmcys.ysm.client.gui.button.FlatIconButton;
import com.elfmcys.ysm.model.resource.client.ModelRenderTarget;
import com.elfmcys.ysm.model.resource.client.ModelResourcePurpose;
import com.elfmcys.ysm.model.resource.client.ResourceLease;
import com.elfmcys.ysm.model.resource.client.ResourceRequest;
import com.elfmcys.ysm.client.demand.ClientDemandCoordinator;
import com.elfmcys.ysm.client.demand.ModelResourceDemand;
import com.elfmcys.ysm.model.service.ClientModelService;
import com.elfmcys.ysm.model.domain.Hash256;
import com.elfmcys.ysm.network.NetworkHandler;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.locale.Language;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PlayerTextureScreen extends ModelScreen {
    private static final String ANIMATION_ANNOTATIONS = "——";
    private static final float SCALE_MAX = 360f;
    private static final float SCALE_MIN = 18f;
    private static final float PITCH_MAX = 90f;
    private static final float PITCH_MIN = -90f;

    protected @Nullable CustomGuiPlayerEntity previewEntity;
    protected final ModelRenderTarget model;
    protected final Hash256 modelHash;
    protected final String modelPath;
    private final PlayerModelScreen parent;
    private final List<String> textures;
    private final List<String> animations = new ArrayList<>();
    private final List<CatalogTextureButton> textureButtons = new ArrayList<>();
    private @Nullable ModelRenderTarget animationSource;
    private com.elfmcys.ysm.client.demand.ClientDemandCoordinator.PageScope pageScope;
    private @Nullable ResourceLease entryLease;
    private String selectedTexture;
    private String animation = AnimationRegister.IDLE;
    private int maxTexturePage;
    private int texturePage;
    private int maxAnimationPage;
    private int animationPage;
    protected int x;
    protected int y;
    protected float posX;
    protected float posY = -60;
    protected float scale = 80;
    protected float yaw = 165;
    protected float pitch = -5;
    protected boolean showGround = true;

    public PlayerTextureScreen(PlayerModelScreen parent, ModelRenderTarget model) {
        super(Component.literal("Player Texture GUI"), parent);
        this.parent = parent;
        this.modelHash = model.modelHash();
        this.model = model;
        var entry = ClientModelService.instance().catalog().find(modelHash)
            .orElseThrow(() -> new IllegalArgumentException("Unknown model hash: " + modelHash));
        this.modelPath = entry.displayPath();
        this.textures = entry.displayRepresentation().view().getPlayer().getTextureNames().stream().sorted().toList();
        this.selectedTexture = model.playerResources().defaultTextureName();
    }

    boolean receiveReady(ResourceRequest request, ResourceLease lease) {
        if (!hasCurrentWorld() || entryLease != null || request.purpose() != ModelResourcePurpose.FULL
            || lease.purpose() != ModelResourcePurpose.FULL || !lease.isCurrent(request))
            return false;
        var receiving = ClientModelService.instance().findReady(request).orElse(null);
        if (receiving == null)
            return false;
        CustomGuiPlayerEntity preview = null;
        try {
            preview = new CustomGuiPlayerEntity(ModelResourcePurpose.FULL);
            var result = preview.receiveReady(request, receiving);
            if (result != ClientDemandCoordinator.InstallationResult.BUSY)
                receiving = null;
            if (result != ClientDemandCoordinator.InstallationResult.INSTALLED)
                return false;
            previewEntity = preview;
            selectedTexture = request.requestedTexture();
            previewEntity.getPreviewInfo().setPreview(AnimationRegister.IDLE);
            entryLease = lease;
            preview = null;
            return true;
        } finally {
            if (receiving != null)
                receiving.close();
            if (preview != null)
                preview.retire();
        }
    }

    @Override
    protected void init() {
        if (!hasCurrentWorld())
            return;
        clearWidgets();
        var signature = pageIdentity();
        if (pageScope == null || !pageScope.matches(signature)) {
            pageScope = ClientModelService.instance().demands().replacePage(pageScope, signature);
            closeTextureButtons();
        }
        x = (width - 420) / 2;
        y = (height - 235) / 2;
        maxTexturePage = Math.max(0, (textures.size() - 1) / 4);
        maxAnimationPage = Math.max(0, (animations.size() - 1) / 11);
        texturePage = Math.min(texturePage, maxTexturePage);
        animationPage = Math.min(animationPage, maxAnimationPage);

        addRenderableWidget(new FlatColorButton(x + 5, y, 80, 18,
            Component.translatable("gui.yes_steve_model.model.return"), ignored -> showScreen(parent)));
        var stopAnimation =
            new FlatIconButton(x + 281, y + 2, 16, 16, 64, 16, ignored -> animation = AnimationRegister.IDLE)
                .setTooltips("gui.yes_steve_model.model.stop");
        stopAnimation.active = animationSource != null;
        addRenderableWidget(stopAnimation);
        addRenderableWidget(new FlatIconButton(x + 263, y + 2, 16, 16, 48, 16, ignored -> {
            posX = 0;
            posY = -60;
            scale = 80;
            yaw = 165;
            pitch = -5;
        }).setTooltips("gui.yes_steve_model.model.reset"));
        addRenderableWidget(new FlatIconButton(x + 245, y + 2, 16, 16, 64, 0, ignored -> showGround = !showGround)
            .setTooltips("gui.yes_steve_model.model.ground"));

        addRenderableWidget(new FlatColorButton(x + 321, y + 213, 18, 18, Component.literal("<"), ignored -> {
            if (texturePage > 0) {
                texturePage--;
                init();
            }
        }));
        addRenderableWidget(new FlatColorButton(x + 383, y + 213, 18, 18, Component.literal(">"), ignored -> {
            if (texturePage < maxTexturePage) {
                texturePage++;
                init();
            }
        }));
        var previousAnimation = new FlatColorButton(x + 11, y + 214, 16, 16, Component.literal("<"), ignored -> {
            if (animationPage > 0) {
                animationPage--;
                init();
            }
        });
        previousAnimation.active = animationPage > 0;
        addRenderableWidget(previousAnimation);
        var nextAnimation = new FlatColorButton(x + 63, y + 214, 16, 16, Component.literal(">"), ignored -> {
            if (animationPage < maxAnimationPage) {
                animationPage++;
                init();
            }
        });
        nextAnimation.active = animationPage < maxAnimationPage;
        addRenderableWidget(nextAnimation);

        addAnimationButtons();
        addTextureButtons();
    }

    private void addAnimationButtons() {
        for (var slot = 0; slot < 11; slot++) {
            var index = slot + animationPage * 11;
            if (index >= animations.size()) {
                break;
            }
            var name = animations.get(index);
            var key = "gui.yes_steve_model.texture.button.%s".formatted(name.replace(':', '.'));
            var keyDesc = key + ".desc";
            var label = Language.getInstance().has(key) ? Component.translatable(key) : Component.literal(name);
            var button = new FlatColorButton(x + 5, y + 27 + 17 * slot, 80, 16, label, ignored -> animation = name);
            if (Language.getInstance().has(keyDesc)) {
                button.setTooltips(Lists.newArrayList(Component.translatable(keyDesc).withStyle(ChatFormatting.GOLD),
                    Component.translatable("gui.yes_steve_model.texture.button.animation_name", name)
                        .withStyle(ChatFormatting.GRAY)));
            }
            addRenderableWidget(button);
        }
    }

    private void addTextureButtons() {
        if (!textureButtons.isEmpty()) {
            for (var slot = 0; slot < textureButtons.size(); slot++) {
                var button = textureButtons.get(slot);
                button.setX(x + 306 + 56 * (slot % 2));
                button.setY(y + 5 + 104 * (slot / 2));
                addRenderableWidget(button);
            }
            return;
        }
        for (var slot = 0; slot < 4; slot++) {
            var index = slot + texturePage * 4;
            if (index >= textures.size()) {
                break;
            }
            var button = new CatalogTextureButton(x + 306 + 56 * (slot % 2), y + 5 + 104 * (slot / 2), modelHash,
                modelPath, textures.get(index), new CustomGuiPlayerEntity(), pageScope, this::selectTexture);
            textureButtons.add(button);
            addRenderableWidget(button);
        }
    }

    protected void selectTexture(Hash256 hash, String path, String texture, @Nullable ModelRenderTarget renderTarget) {
        if (!hasCurrentWorld() || previewEntity == null)
            return;
        selectedTexture = texture;
        previewEntity.setResourceDemandMode(ModelResourceDemand.Mode.ONLINE);
        previewEntity.updateModelAndTexture(hash, texture);
        parent.selectModel(hash, path, texture, renderTarget == null ? model : renderTarget);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (!hasCurrentWorld() || previewEntity == null)
            return;
        updatePointer(mouseX, mouseY);
        graphics.fillGradient(x, y + 22, x + 90, y + 235, 0xFF222222, 0xFF222222);
        graphics.fillGradient(x + 93, y, x + 299, y + 235, 0xFF222222, 0xFF222222);
        graphics.fillGradient(x + 302, y, x + 420, y + 235, 0xFF222222, 0xFF222222);

        if (!previewEntity.getPreviewInfo().hasPreview(animation)) {
            previewEntity.getPreviewInfo().setPreview(animation);
        }
        renderReferenceEntity(graphics, x + 93, y, 206, 235, minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true));

        var texturePageInfo = "%d/%d".formatted(texturePage + 1, maxTexturePage + 1);
        graphics.text(font, texturePageInfo, x + 302 + (118 - font.width(texturePageInfo)) / 2,
            y + 223 - font.lineHeight / 2, 0xFFF3EFE0);
        var animationPageInfo = "%d/%d".formatted(animationPage + 1, maxAnimationPage + 1);
        graphics.text(font, animationPageInfo, x + 5 + (80 - font.width(animationPageInfo)) / 2, y + 218,
            0xFFF3EFE0);
        if (animationSource == null) {
            var failed = previewEntity.modelLoadFailure() != null;
            graphics.centeredText(font,
                Component.translatable(failed ? "gui.yes_steve_model.texture.animation_failed"
                    : "gui.yes_steve_model.texture.animation_loading"),
                x + 45, y + 110, failed ? 0xFFFF5555 : 0xFFF3EFE0);
        }
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        children().stream().filter(FlatColorButton.class::isInstance).map(FlatColorButton.class::cast)
            .forEach(button -> button.renderToolTip(graphics, this, mouseX, mouseY));
    }

    @Override
    public void tick() {
        if (!hasCurrentWorld())
            return;
        if (pageScope == null || !pageScope.matches(pageIdentity()))
            init();
        if (previewEntity != null)
            refreshAnimationList();
    }

    private List<?> pageIdentity() {
        var content = ClientModelService.instance().catalog().find(modelHash)
            .map(entry -> (Object)entry.content().representation().identity()).orElse("missing");
        return List.of(modelHash, content, texturePage);
    }

    private void refreshAnimationList() {
        var ready = previewEntity.displayedModel();
        if (ready == null || ready == animationSource)
            return;
        animationSource = ready;
        var next = ready.playerResources().animations().keySet().stream()
            .filter(name -> !name.startsWith(ANIMATION_ANNOTATIONS)).sorted().toList();
        if (!animations.equals(next)) {
            animations.clear();
            animations.addAll(next);
            init();
        }
    }

    protected void renderReferenceEntity(GuiGraphicsExtractor graphics, int scissorX, int scissorY, int scissorW, int scissorH,
        float partialTick) {
        GuiModelPreview.textureScene(graphics, scissorX, scissorY, scissorW, scissorH,
            x + 299 / 2.0F + 40 + posX, y + 235 / 2.0F + 80 + posY, scale, pitch, yaw, partialTick,
            previewEntity, showGround);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
        updatePointer(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        updatePointer(event.x(), event.y());
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        var handled = super.keyPressed(event);
        updateKeyboardPointer();
        return handled;
    }

    @Override
    public void resize(int width, int height) {
        var focused = getFocused();
        super.resize(width, height);
        if (textureButtons.contains(focused))
            setFocused(focused);
    }

    private void updateKeyboardPointer() {
        if (pageScope != null)
            pageScope.pointer(getFocused() instanceof CatalogTextureButton card ? card.target() : null);
    }

    private void updatePointer(double mouseX, double mouseY) {
        if (minecraft != null && minecraft.getLastInputType().isKeyboard())
            updateKeyboardPointer();
        else if (pageScope != null)
            pageScope.pointer(textureButtons.stream().filter(card -> card.isMouseOver(mouseX, mouseY))
                .map(CatalogTextureButton::target).findFirst().orElse(null));
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (!inViewRange(mouseX, mouseY)) {
            return false;
        }
        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            yaw += (float)(1.5 * dragX);
            pitch = Mth.clamp(pitch - (float)dragY, PITCH_MIN, PITCH_MAX);
        } else if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
            posX += (float)dragX;
            posY += (float)dragY;
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double delta) {
        if (delta == 0) {
            return super.mouseScrolled(mouseX, mouseY, horizontal, delta);
        }
        if (inViewRange(mouseX, mouseY)) {
            scale = Mth.clamp(scale + (float)delta * 0.07f * scale, SCALE_MIN, SCALE_MAX);
            return true;
        }
        if (inAnimationRange(mouseX, mouseY)) {
            var next = page(animationPage, maxAnimationPage, delta);
            if (next != animationPage) {
                animationPage = next;
                init();
            }
            return true;
        }
        if (inTextureRange(mouseX, mouseY)) {
            var next = page(texturePage, maxTexturePage, delta);
            if (next != texturePage) {
                texturePage = next;
                init();
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, delta);
    }

    @Override
    public void removed() {
        releaseResources();
        super.removed();
    }

    void releaseResources() {
        closePage();
        var preview = previewEntity;
        var lease = entryLease;
        previewEntity = null;
        entryLease = null;
        try {
            if (preview != null)
                preview.retire();
        } finally {
            animationSource = null;
            animations.clear();
            if (lease != null)
                lease.close();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void closeTextureButtons() {
        textureButtons.forEach(CatalogTextureButton::close);
        textureButtons.clear();
    }

    private void closePage() {
        if (pageScope != null)
            pageScope.close();
        pageScope = null;
        closeTextureButtons();
    }

    private int page(int current, int maximum, double delta) {
        var next = delta > 0 ? Math.max(0, current - 1) : Math.min(maximum, current + 1);
        if (next != current) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
        }
        return next;
    }

    private boolean inViewRange(double mouseX, double mouseY) {
        return mouseX > x + 93 && mouseX < x + 299 && mouseY > y && mouseY < y + 235;
    }

    private boolean inAnimationRange(double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + 90 && mouseY > y + 22 && mouseY < y + 235;
    }

    private boolean inTextureRange(double mouseX, double mouseY) {
        return mouseX > x + 302 && mouseX < x + 420 && mouseY > y && mouseY < y + 235;
    }
}
