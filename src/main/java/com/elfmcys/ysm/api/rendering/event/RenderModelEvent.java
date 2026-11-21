package com.elfmcys.ysm.api.rendering.event;

import com.elfmcys.ysm.api.event.CancellableEvent;
import com.elfmcys.ysm.geckolib3.geo.GeoRenderData;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;

/** The collector and pose are borrowed during this event's synchronous dispatch. */
public class RenderModelEvent extends CancellableEvent {
    private final Object target;
    private final GeoRenderData renderData;
    private final SubmitNodeCollector collector;
    private final RenderType renderType;
    private final PoseStack poseStack;
    private final int light;
    private final int overlay;
    private final int color;

    public RenderModelEvent(Object target, GeoRenderData renderData, SubmitNodeCollector collector, RenderType renderType,
        PoseStack poseStack, int light, int overlay, int color) {
        this.target = target;
        this.renderData = renderData;
        this.collector = collector;
        this.renderType = renderType;
        this.poseStack = poseStack;
        this.light = light;
        this.overlay = overlay;
        this.color = color;
    }

    /// 当前只有 Entity，以后说不定有别的
    public Object target() {
        return target;
    }

    public GeoRenderData renderData() {
        return renderData;
    }

    public SubmitNodeCollector collector() {
        return collector;
    }

    public RenderType renderType() {
        return renderType;
    }

    public PoseStack pose() {
        return poseStack;
    }

    public int light() {
        return light;
    }

    public int overlay() {
        return overlay;
    }

    public int color() {
        return color;
    }
}
