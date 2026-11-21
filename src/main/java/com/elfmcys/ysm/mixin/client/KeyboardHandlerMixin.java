package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.platform.input.ClientInputHooks;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * PORT(26.3-fabric): replaces Forge {@code InputEvent.Key}, which Forge posts after vanilla key handling.
 * Target verified against the 26.3 jar: {@code keyPress(JILnet/minecraft/client/input/KeyEvent;)V}, parameters
 * (handle, action, event). The method returns early for other windows, so the handle is checked here.
 */
@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "keyPress(JILnet/minecraft/client/input/KeyEvent;)V", at = @At("RETURN"))
    private void ysmKeyPress(long handle, int action, KeyEvent event, CallbackInfo ci) {
        if (handle != 0L && handle == this.minecraft.getWindow().handle()) {
            ClientInputHooks.fireKey(event, action);
        }
    }
}
