package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.client.gui.GuiModelPreview;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {
    @WrapWithCondition(method = "extractEntityInInventoryFollowsMouse"
        + "(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIIIFFFLnet/minecraft/world/entity/LivingEntity;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;entity"
            + "(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;FLorg/joml/Vector3fc;"
            + "Lorg/joml/Quaternionfc;Lorg/joml/Quaternionfc;IIII)V"))
    private static boolean ysm$extractPlayer(GuiGraphicsExtractor graphics, EntityRenderState state, float scale,
        Vector3fc translation, Quaternionfc rotation, Quaternionfc camera, int left, int top, int right, int bottom,
        @Local(argsOnly = true) LivingEntity entity) {
        return !(entity instanceof Player player && state instanceof AvatarRenderState avatar)
            || !GuiModelPreview.localPlayer(graphics, player, avatar, scale, translation, rotation,
                left, top, right, bottom, false, 1.0F);
    }
}
