package com.elfmcys.ysm.client.renderer;

import com.elfmcys.ysm.client.entity.CustomPlayerEntity;
import com.elfmcys.ysm.client.model.locator.PlayerLocator;
import com.elfmcys.ysm.geckolib3.geo.GeoRenderData;
import com.elfmcys.ysm.geckolib3.geo.render.built.GeoLocator;
import com.elfmcys.ysm.mixin.client.ParrotOnShoulderLayerAccessor;
import com.elfmcys.ysm.util.EquipmentUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.ParrotOnShoulderLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class CustomPlayerRenderer {
    private final EntityRendererProvider.Context context;
    private final ItemModelResolver items;
    private WingsLayer<AvatarRenderState, PlayerModel> wings;
    private ParrotOnShoulderLayer parrots;

    public CustomPlayerRenderer(EntityRendererProvider.Context context) {
        this.context = context;
        items = context.getItemModelResolver();
    }

    public void submitLayers(CustomPlayerEntity animatable, GeoRenderData data, AvatarRenderer<?> renderer,
        AvatarRenderState state, PoseStack pose, SubmitNodeCollector collector, int overlay) {
        var player = animatable.getEntity();
        var locators = PlayerLocator.get();
        submitItem(player, player.getMainHandItem(), ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, data, pose, collector,
            locators.rightHand, state, overlay);
        submitItem(player, player.getOffhandItem(), ItemDisplayContext.THIRD_PERSON_LEFT_HAND, data, pose, collector,
            locators.leftHand, state, overlay);

        var head = player.getItemBySlot(EquipmentSlot.HEAD);
        var equipment = head.get(DataComponents.EQUIPPABLE);
        if (equipment == null || equipment.slot() != EquipmentSlot.HEAD || equipment.assetId().isEmpty()) {
            submitItem(player, head, ItemDisplayContext.HEAD, data, pose, collector, locators.head, state, overlay);
        }

        var elytra = EquipmentUtil.getEquippedElytraItem(player);
        if (!elytra.isEmpty() && data.modelState.locatorGroupSize(locators.elytra) > 0) {
            if (wings == null) {
                wings = new WingsLayer<>(renderer, context.getModelSet(), context.getEquipmentRenderer());
            }
            var previousChest = state.chestEquipment;
            state.chestEquipment = elytra;
            try {
                data.modelState.visitLocatorGroup(locators.elytra, pose, located -> {
                    located.translate(0, 1.5, 0);
                    located.rotateDegrees(Axis.ZP, 180);
                    located.scale(2, 2, 2);
                    wings.submit(located, collector, state.lightCoords, state, state.yRot, state.xRot);
                });
            } finally {
                state.chestEquipment = previousChest;
            }
        }

        if (state.parrotOnLeftShoulder != null || state.parrotOnRightShoulder != null) {
            if (parrots == null) {
                parrots = new ParrotOnShoulderLayer(renderer, context.getModelSet());
            }
            if (state.parrotOnLeftShoulder != null) {
                data.modelState.visitLocatorGroup(locators.leftShoulder, pose, located ->
                    submitParrot(located, collector, state, true));
            }
            if (state.parrotOnRightShoulder != null) {
                data.modelState.visitLocatorGroup(locators.rightShoulder, pose, located ->
                    submitParrot(located, collector, state, false));
            }
        }
    }

    private void submitParrot(PoseStack pose, SubmitNodeCollector collector, AvatarRenderState state, boolean left) {
        pose.translate(0, 1.5, 0);
        pose.rotateDegrees(Axis.ZP, 180);
        pose.translate(left ? -0.4F : 0.4F, state.isCrouching ? 1.3F : 1.5F, 0);
        ((ParrotOnShoulderLayerAccessor) parrots).ysm$submitShoulder(pose, collector, state.lightCoords, state,
            left ? state.parrotOnLeftShoulder : state.parrotOnRightShoulder, state.yRot, state.xRot, left);
    }

    private void submitItem(LivingEntity player, ItemStack item, ItemDisplayContext display, GeoRenderData data,
        PoseStack pose, SubmitNodeCollector collector, GeoLocator locator, AvatarRenderState avatar, int overlay) {
        if (item.isEmpty() || data.modelState.locatorGroupSize(locator) == 0) {
            return;
        }
        var state = new ItemStackRenderState();
        items.updateForLiving(state, item, display, player);
        data.modelState.visitLocatorGroup(locator, pose, located -> {
            if (display == ItemDisplayContext.HEAD) {
                located.scale(0.625F, 0.625F, 0.625F);
                located.translate(0, 0.25F, 0);
            } else {
                located.translate(0, -0.0625F, -0.1F);
                located.rotateDegrees(Axis.XP, -90);
            }
            state.submit(located, collector, avatar.lightCoords, overlay, avatar.outlineColor);
        });
    }
}
