package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.client.event.EntityLoadEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class ClientLevelUnloadMixin {
    @Shadow public ClientLevel level;

    @Inject(method = "setLevel(Lnet/minecraft/client/multiplayer/ClientLevel;)V", at = @At("HEAD"))
    private void ysm$beforeLevelChange(ClientLevel nextLevel, CallbackInfo ci) {
        if (level != null && level != nextLevel) {
            EntityLoadEvent.onClientLevelUnload(level);
        }
    }

    @Inject(method = "clearClientLevel(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"))
    private void ysm$beforeLevelClear(Screen screen, CallbackInfo ci) {
        if (level != null) {
            EntityLoadEvent.onClientLevelUnload(level);
        }
    }
}
