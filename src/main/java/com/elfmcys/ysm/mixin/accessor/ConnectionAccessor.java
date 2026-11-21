package com.elfmcys.ysm.mixin.accessor;

import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** PORT(26.3-fabric): {@code Connection.channel()} is gone and the field is private (verified in the 26.3 jar). */
@Mixin(Connection.class)
public interface ConnectionAccessor {
    @Accessor("channel")
    Channel ysm$getChannel();
}
