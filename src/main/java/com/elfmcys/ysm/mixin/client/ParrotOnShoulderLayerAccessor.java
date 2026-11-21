package com.elfmcys.ysm.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ParrotOnShoulderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.animal.parrot.Parrot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ParrotOnShoulderLayer.class)
public interface ParrotOnShoulderLayerAccessor {
    @Invoker("submitOnShoulder")
    void ysm$submitShoulder(PoseStack pose, SubmitNodeCollector collector, int light, AvatarRenderState state,
        Parrot.Variant variant, float yaw, float pitch, boolean left);
}
