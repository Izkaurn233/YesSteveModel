package com.elfmcys.ysm.api.rendering.event;

import com.elfmcys.ysm.api.event.CancellableEvent;
import com.elfmcys.ysm.geckolib3.geo.GeoRenderData;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;

/** The collector and pose are borrowed during this event's synchronous dispatch. */
public class RenderLayerEvent extends CancellableEvent {
    private final Object target;
    private final GeoRenderData renderData;
    private final PoseStack poseStack;
    private final SubmitNodeCollector collector;
    private final int packedLight;
    private final int overlay;

    public RenderLayerEvent(Object target, GeoRenderData renderData, PoseStack poseStack, SubmitNodeCollector collector,
        int packedLight, int overlay) {
        this.target = target;
        this.renderData = renderData;
        this.poseStack = poseStack;
        this.collector = collector;
        this.packedLight = packedLight;
        this.overlay = overlay;
    }

    public Object target() {
        return target;
    }

    public GeoRenderData renderData() {
        return renderData;
    }

    public PoseStack poseStack() {
        return poseStack;
    }

    public SubmitNodeCollector collector() {
        return collector;
    }

    public int packedLight() {
        return packedLight;
    }

    public int overlay() {
        return overlay;
    }
}
