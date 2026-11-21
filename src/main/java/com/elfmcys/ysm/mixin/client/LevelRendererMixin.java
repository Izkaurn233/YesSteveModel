package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.animation.AnimationParallelTicker;
import com.elfmcys.ysm.util.RenderUtil;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @WrapMethod(method = "submitFeatures(Lnet/minecraft/client/renderer/state/level/LevelRenderState;"
        + "Lnet/minecraft/client/renderer/SubmitNodeCollector;Z)V")
    private void ysm$submitLevelFeatures(LevelRenderState state, SubmitNodeCollector collector, boolean shouldRenderEntities,
        Operation<Void> original) {
        if (!YesSteveModel.isAvailable()) {
            original.call(state, collector, shouldRenderEntities);
            return;
        }
        RenderUtil.setRenderingLevel(true);
        try {
            AnimationParallelTicker.scheduleAll(state.cameraRenderState.cameraEntityPartialTicks);
            original.call(state, collector, shouldRenderEntities);
        } finally {
            try {
                AnimationParallelTicker.waitAll();
            } finally {
                RenderUtil.setRenderingLevel(false);
            }
        }
    }
}
