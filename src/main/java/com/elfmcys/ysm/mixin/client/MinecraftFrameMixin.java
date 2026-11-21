package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.natives.render.NativeRenderer;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * PORT(26.3-fabric): starts a frame for the native vertex arena. Target verified against the 26.3 jar:
 * {@code public void renderFrame(Z)V}, which both extracts and renders the frame (GUI and level), so every custom
 * geometry callback submitted in a frame runs before the next head.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftFrameMixin {
    @Inject(method = "renderFrame(Z)V", at = @At("HEAD"))
    private void ysm$beginNativeVertexFrame(boolean advanceGameTime, CallbackInfo ci) {
        NativeRenderer.beginFrame();
    }
}
