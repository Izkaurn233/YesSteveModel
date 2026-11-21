package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.client.entity.FirstPersonPart;
import com.elfmcys.ysm.client.event.ReplacePlayerHandRenderEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class FirstPersonHandsMixin {
    @Inject(method = "renderPlayerHand(Lcom/mojang/blaze3d/vertex/PoseStack;"
        + "Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/world/entity/HumanoidArm;"
        + "Lnet/minecraft/client/renderer/state/level/PlayerRenderState;)V", at = @At("HEAD"), cancellable = true)
    private void ysm$replaceArm(PoseStack pose, SubmitNodeCollector collector, int light, HumanoidArm arm,
        PlayerRenderState player, CallbackInfo ci) {
        if (ReplacePlayerHandRenderEvent.submit(FirstPersonPart.forArm(arm), pose, collector, light,
            OverlayTexture.NO_OVERLAY)) {
            ci.cancel();
        }
    }
}
