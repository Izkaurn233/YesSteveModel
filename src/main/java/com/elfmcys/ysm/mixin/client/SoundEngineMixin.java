package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.client.sound.instance.CustomSoundInstance;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {
    @WrapOperation(method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)"
        + "Lnet/minecraft/client/sounds/SoundEngine$PlayResult;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundBufferLibrary;"
            + "getStream(Lnet/minecraft/resources/Identifier;Z)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<AudioStream> ysm$loadCustomStream(SoundBufferLibrary library, Identifier id, boolean looping,
        Operation<CompletableFuture<AudioStream>> original, @Local(argsOnly = true) SoundInstance instance) {
        return instance instanceof CustomSoundInstance custom ? custom.getStream() : original.call(library, id, looping);
    }
}
