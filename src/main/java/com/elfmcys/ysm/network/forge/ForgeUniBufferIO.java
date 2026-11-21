package com.elfmcys.ysm.network.forge;

import com.elfmcys.ysm.buffer.NativeStorage;
import com.elfmcys.ysm.buffer.ArrayBuffer;
import com.elfmcys.ysm.buffer.NativeBuffer;
import com.elfmcys.ysm.buffer.UniBuffer;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;

import java.lang.ref.Reference;

public final class ForgeUniBufferIO {
    private ForgeUniBufferIO() {}

    public static void write(FriendlyByteBuf target, UniBuffer source) {
        try {
            if (source instanceof ArrayBuffer array) {
                target.writeBytes(array.array(), array.arrayOffset(), array.size());
                return;
            }
            target.writeBytes(source.nio());
        } finally {
            Reference.reachabilityFence(source);
            Reference.reachabilityFence(target);
        }
    }

    public static NativeStorage readNative(FriendlyByteBuf source, int length) {
        if (length < 0 || length > source.readableBytes()) {
            throw new IndexOutOfBoundsException();
        }
        var target = NativeBuffer.allocate(length);
        try {
            copyFromNetty(source, source.readerIndex(), target, 0, length);
            source.skipBytes(length);
            return target;
        } catch (Throwable error) {
            target.close();
            throw error;
        }
    }

    static void copyFromNetty(ByteBuf source, int sourceIndex, NativeBuffer target, int targetOffset, int length) {
        try {
            if (sourceIndex < 0 || targetOffset < 0 || length < 0 || sourceIndex > source.writerIndex() - length
                || targetOffset > target.size() - length) {
                throw new IndexOutOfBoundsException();
            }
            var view = target.nio();
            view.position(targetOffset).limit(targetOffset + length);
            source.getBytes(sourceIndex, view);
        } finally {
            Reference.reachabilityFence(source);
            Reference.reachabilityFence(target);
        }
    }
}
