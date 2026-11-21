package com.elfmcys.ysm.mixin.accessor;

import net.minecraft.network.Connection;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** PORT(26.3-fabric): {@code ServerCommonPacketListenerImpl.connection} is protected in 26.3. */
@Mixin(ServerCommonPacketListenerImpl.class)
public interface ServerCommonPacketListenerAccessor {
    @Accessor("connection")
    Connection ysm$getConnection();
}
