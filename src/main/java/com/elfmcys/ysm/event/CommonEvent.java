package com.elfmcys.ysm.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.model.ModelRuntime;
import com.elfmcys.ysm.network.NetworkHandler;
import com.elfmcys.ysm.platform.Platform;

/**
 * PORT(26.3-fabric): Forge FMLCommonSetupEvent work, called from {@code YesSteveModelFabric} after
 * {@code YesSteveModel.init()}. Fabric runs the main entrypoint before the client one, the same order as Forge
 * common setup before client setup. Capability type registration (RegisterCapabilitiesEvent) moved to
 * {@code CapabilityBootstrap}. The Forge mod-loading warning for an unavailable native library has no Fabric
 * screen; {@code YesSteveModel.init()} logs it instead.
 */
public final class CommonEvent {
    private CommonEvent() {
    }

    public static void onSetup() {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        ModelRuntime.initialize();
        NetworkHandler.init();
        if (Platform.isDedicatedServer()) {
            ModelRuntime.system().catalog().activate();
        }
    }
}
