package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.texture.YsmTexture;
import com.elfmcys.ysm.util.ResourceDiagnostics;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextureManager.class)
public abstract class TextureManagerMixin {
    @Shadow @Final private Map<Identifier, AbstractTexture> byPath;
    @Shadow @Final private ResourceManager resourceManager;

    @Inject(
        method = "register(Lnet/minecraft/resources/Identifier;Lnet/minecraft/client/renderer/texture/AbstractTexture;)V",
        at = @At("HEAD"))
    private void ysm$loadBeforeRegister(Identifier id, AbstractTexture texture, CallbackInfo ci) {
        if (texture instanceof YsmTexture owned && byPath.get(id) != texture) {
            try {
                owned.load(resourceManager);
            } catch (IOException failure) {
                throw new UncheckedIOException("Failed to load YSM texture " + id, failure);
            }
        }
    }

    @Inject(
        method = "register(Lnet/minecraft/resources/Identifier;Lnet/minecraft/client/renderer/texture/AbstractTexture;)V",
        at = @At("RETURN"))
    private void ysm$afterRegister(Identifier id, AbstractTexture texture, CallbackInfo ci) {
        if (texture instanceof YsmTexture && byPath.get(id) == texture) {
            ResourceDiagnostics.track(ResourceDiagnostics.Kind.TEXTURE, texture, id.toString());
            ResourceDiagnostics.log("texture register: {} type={}", id, texture.getClass().getSimpleName());
        }
    }

    @Inject(method = "safeClose(Lnet/minecraft/resources/Identifier;Lnet/minecraft/client/renderer/texture/AbstractTexture;)V",
        at = @At("RETURN"))
    private void ysm$afterClose(Identifier id, AbstractTexture texture, CallbackInfo ci) {
        if (texture instanceof YsmTexture) {
            ResourceDiagnostics.untrack(ResourceDiagnostics.Kind.TEXTURE, texture);
            ResourceDiagnostics.log("texture remove: {}", id);
        }
    }

    @Inject(method = "reload(Lnet/minecraft/server/packs/resources/PreparableReloadListener$SharedState;"
        + "Ljava/util/concurrent/Executor;Lnet/minecraft/server/packs/resources/PreparableReloadListener$PreparationBarrier;"
        + "Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;",
        at = @At("RETURN"), cancellable = true)
    private void ysm$reloadOwnedTextures(PreparableReloadListener.SharedState state, Executor prepare,
        PreparableReloadListener.PreparationBarrier barrier, Executor apply,
        CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        var textures = byPath.entrySet().stream()
            .filter(entry -> entry.getValue() instanceof YsmTexture)
            .map(entry -> Map.entry(entry.getKey(), (YsmTexture) entry.getValue()))
            .toList();
        if (!textures.isEmpty()) {
            cir.setReturnValue(cir.getReturnValue().thenRunAsync(() -> {
                for (var entry : textures) {
                    if (byPath.get(entry.getKey()) != entry.getValue()) {
                        continue;
                    }
                    try {
                        entry.getValue().load(state.resourceManager());
                    } catch (IOException | RuntimeException failure) {
                        YesSteveModel.LOGGER.error("Failed to reload YSM texture {}", entry.getKey(), failure);
                    }
                }
            }, apply));
        }
    }
}
