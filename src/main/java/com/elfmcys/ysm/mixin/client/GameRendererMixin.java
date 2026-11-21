package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.client.event.RenderFirstPlayerBackground;
import com.elfmcys.ysm.client.event.ReplacePlayerHandRenderEvent;
import com.elfmcys.ysm.natives.NativeProfiler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow @Final private Projection hudProjection;

    @Unique
    private boolean ysm$profileFrameActive;

    @WrapOperation(method = "renderItemInHand(Lnet/minecraft/client/renderer/state/level/CameraRenderState;"
        + "Lnet/minecraft/client/renderer/state/level/PlayerRenderState;"
        + "Lcom/mojang/renderpearl/api/textures/GpuTextureView;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/FirstPersonHandsAndItemsRenderer;"
            + "submitHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;"
            + "Lnet/minecraft/client/renderer/state/level/PlayerRenderState;"
            + "Lnet/minecraft/client/renderer/state/level/FirstPersonHandsAndItemsRenderState;)V"))
    private void ysm$submitHands(FirstPersonHandsAndItemsRenderer renderer, float partialTick, PoseStack pose,
        SubmitNodeCollector collector, PlayerRenderState player, FirstPersonHandsAndItemsRenderState hands,
        Operation<Void> original) {
        var camera = new CameraRenderState();
        hudProjection.getMatrix(camera.projectionMatrix);
        camera.viewRotationMatrix.set(RenderSystem.getModelViewMatrixCopy());
        ReplacePlayerHandRenderEvent.inHandPass(camera, partialTick, () -> {
            RenderFirstPlayerBackground.submit(pose, collector, player.avatarRenderState.lightCoords, partialTick);
            original.call(renderer, partialTick, pose, collector, player, hands);
        });
    }

    @Inject(method = "render()V", at = @At("HEAD"))
    private void beforeRender(CallbackInfo ci) {
        ysm$profileFrameActive = NativeProfiler.beginFrame();
    }

    @Inject(method = "render()V", at = @At("RETURN"))
    private void afterRender(CallbackInfo ci) {
        NativeProfiler.endFrame(ysm$profileFrameActive);
        ysm$profileFrameActive = false;
    }
}
