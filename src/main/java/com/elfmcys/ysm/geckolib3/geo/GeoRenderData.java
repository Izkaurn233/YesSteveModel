package com.elfmcys.ysm.geckolib3.geo;

import com.elfmcys.ysm.geckolib3.geo.animated.GeoModelState;
import com.elfmcys.ysm.geckolib3.model.provider.data.EntityModelData;
import net.minecraft.resources.Identifier;

public class GeoRenderData {
    public final GeoModelState modelState = new GeoModelState();

    public Object owner;
    public Object installation;
    public Identifier texture;
    public float widthScale;
    public float heightScale;
    public boolean renderLayersFirst;
    public RenderContext ctx;

    public float partialTicks;
    public EntityModelData animationData;
}
