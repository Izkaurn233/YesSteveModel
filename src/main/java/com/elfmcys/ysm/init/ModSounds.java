package com.elfmcys.ysm.init;

import com.elfmcys.ysm.YesSteveModel;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
    public static final SoundEvent CUSTOM =
        SoundEvent.createFixedRangeEvent(Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "custom"), 16.0F);

    /**
     * PORT(26.3-fabric): replaces the Forge DeferredRegister, registering directly into the built-in registry
     * during mod init. Upstream registered sounds on the client only, and that is kept here.
     * TODO: not verified: Fabric registry sync's behaviour for a client-only SOUND_EVENT entry when joining a
     * server without YSM.
     */
    public static void register() {
        Registry.register(BuiltInRegistries.SOUND_EVENT, CUSTOM.location(), CUSTOM);
    }
}
