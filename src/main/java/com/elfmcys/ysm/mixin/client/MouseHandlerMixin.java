package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.platform.input.ClientInputHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * PORT(26.3-fabric): replaces Forge {@code InputEvent.MouseButton.Post}.
 * Target verified against the 26.3 jar: {@code onButton(JLnet/minecraft/client/input/MouseButtonInfo;I)V},
 * parameters (handle, rawButtonInfo, action).
 * TODO: semantics not confirmed. Whether vanilla rewrites the raw button before dispatch (the "raw" prefix suggests
 * it might) has not been read from the method body; YSM consumes the raw value.
 */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "onButton(JLnet/minecraft/client/input/MouseButtonInfo;I)V", at = @At("RETURN"))
    private void ysmOnButton(long handle, MouseButtonInfo rawButtonInfo, int action, CallbackInfo ci) {
        if (handle != 0L && handle == this.minecraft.getWindow().handle()) {
            ClientInputHooks.fireMouseButton(rawButtonInfo, action);
        }
    }
}
