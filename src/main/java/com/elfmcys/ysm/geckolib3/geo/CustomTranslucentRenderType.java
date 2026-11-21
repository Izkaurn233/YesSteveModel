package com.elfmcys.ysm.geckolib3.geo;

import com.elfmcys.ysm.mixin.client.RenderTypeInvoker;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

public final class CustomTranslucentRenderType {
    private static final Function<Identifier, RenderType> CUSTOM_TRANSLUCENT = Util.memoize(texture ->
        RenderTypeInvoker.ysm$create("entity_translucent_ysm", RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT)
            .setOitPipelines(RenderPipelines.OIT_ENTITY)
            .withTexture("Sampler0", texture)
            .useLightmap()
            .useOverlay()
            .affectsCrumbling()
            .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
            .createRenderSetup()));

    private CustomTranslucentRenderType() {}

    public static RenderType create(Identifier textureLocation) {
        return CUSTOM_TRANSLUCENT.apply(textureLocation);
    }
}
