package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.gui.DownloadScreen;
import com.elfmcys.ysm.client.gui.PlayerModelScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class DownloadScreenInterModEvent {
    private static final String ENTRYPOINT = "ysm:download_screen";

    private DownloadScreenInterModEvent() {}

    public static void openDownloadScreen(PlayerModelScreen modelScreen) {
        if (!modelScreen.hasCurrentWorld()) {
            return;
        }
        for (var entrypoint : FabricLoader.getInstance().getEntrypointContainers(ENTRYPOINT, Factory.class)) {
            try {
                var screen = entrypoint.getEntrypoint().create(modelScreen);
                if (screen != null) {
                    modelScreen.showScreen(screen);
                    return;
                }
            } catch (RuntimeException | LinkageError failure) {
                YesSteveModel.LOGGER.error("Failed to open a download screen from a Fabric entrypoint", failure);
            }
        }
        modelScreen.showScreen(new DownloadScreen(modelScreen));
    }

    @FunctionalInterface
    public interface Factory {
        @Nullable Screen create(PlayerModelScreen parent);
    }
}
