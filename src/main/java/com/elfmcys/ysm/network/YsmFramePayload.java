package com.elfmcys.ysm.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * One complete YSM frame (protocol-v1 simple or full frame) carried as a vanilla custom payload on
 * {@link NetworkHandler#CHANNEL_NAME}.
 * <p>
 * PORT(26.3-fabric): replaces the Forge {@code EventNetworkChannel} raw payload. The codec copies the frame bytes and
 * does not interpret them; {@code FrameCodec} validates on receipt as before. Frames are bounded by
 * {@code FrameCodec.MAX_BODY_BYTES} (30 KiB), under vanilla's serverbound custom payload limit, so no splitting is needed.
 */
public record YsmFramePayload(byte[] frame) implements CustomPacketPayload {
    public static final Type<YsmFramePayload> TYPE = new Type<>(NetworkHandler.CHANNEL_NAME);
    public static final StreamCodec<FriendlyByteBuf, YsmFramePayload> CODEC =
        CustomPacketPayload.codec(YsmFramePayload::write, YsmFramePayload::read);

    private static YsmFramePayload read(FriendlyByteBuf buffer) {
        var bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        return new YsmFramePayload(bytes);
    }

    private void write(FriendlyByteBuf buffer) {
        buffer.writeBytes(frame);
    }

    @Override
    public Type<YsmFramePayload> type() {
        return TYPE;
    }
}
