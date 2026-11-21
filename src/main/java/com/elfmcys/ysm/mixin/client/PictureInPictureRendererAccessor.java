package com.elfmcys.ysm.mixin.client;

import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PictureInPictureRenderer.class)
public interface PictureInPictureRendererAccessor {
    @Accessor("submitNodeStorage")
    SubmitNodeStorage ysm$getSubmitNodeStorage();

    @Mutable
    @Accessor("submitNodeStorage")
    void ysm$setSubmitNodeStorage(SubmitNodeStorage storage);
}
