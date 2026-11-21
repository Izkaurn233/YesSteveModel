package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.client.gui.ModelScreen;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiScreenMixin {
    @Inject(method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"), cancellable = true)
    private void ysm$checkScreenWorld(Screen screen, CallbackInfo ci) {
        if (!ModelScreen.allowOpening(screen)) {
            ci.cancel();
        }
    }
}
