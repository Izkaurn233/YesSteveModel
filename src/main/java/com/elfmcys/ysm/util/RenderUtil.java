package com.elfmcys.ysm.util;

import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.capability.VehicleAnimatableCapabilityProvider;
import com.elfmcys.ysm.client.compat.FirstPersonCompat;
import com.elfmcys.ysm.client.compat.IrisCompat;
import com.elfmcys.ysm.geckolib3.geo.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;


@SuppressWarnings("all")
public final class RenderUtil {
    private static boolean renderingInInventory = false;
    private static boolean renderingInPaperDoll = false;
    private static boolean renderingLevel = false;

    public static void setRenderingInInventory(boolean value) {
        renderingInInventory = value;
    }

    public static void setRenderingInPaperDoll(boolean renderingEntitiesInPaperDoll) {
        RenderUtil.renderingInPaperDoll = renderingEntitiesInPaperDoll;
    }

    public static void setRenderingLevel(boolean renderingLevel) {
        RenderUtil.renderingLevel = renderingLevel;
    }

    public static boolean isRenderingLevel() {
        RenderSystem.assertOnRenderThread();
        return renderingLevel;
    }

    public static RenderContext extractRenderContext() {
        RenderSystem.assertOnRenderThread();
        return new RenderContext(renderingLevel && !FirstPersonCompat.isRenderingPlayer(),
            IrisCompat.isRenderingShadow(), FirstPersonCompat.isRenderingPlayer(), renderingInInventory,
            renderingInPaperDoll, false, false);
    }

    public static void adjustPassengerPosition(Entity entity, PoseStack poseStack) {
        Entity vehicle = entity.getVehicle();
        if (vehicle != null) {
            vehicle.getCapability(VehicleAnimatableCapabilityProvider.CAP).ifPresent(vehicleCap -> {
                if (!vehicleCap.isAssigned() || !vehicleCap.hasMatchingInstallation()) {
                    return;
                }
                if (!vehicle.getPassengers().contains(entity)) {
                    return;
                }
                var seat = vehicle.getPassengerRidingPosition(entity);
                var attachment = entity.getVehicleAttachmentPoint(vehicle);
                double yOffset = vehicle.getY() - seat.y + attachment.y;
                boolean playerHasCap = entity instanceof Player player
                    && player.getCapability(PlayerAnimatableCapabilityProvider.CAP).isPresent();
                if (playerHasCap) {
                    yOffset = yOffset - 0.5;
                }
                poseStack.translate(0, yOffset, 0);
            });
        }
    }

}
