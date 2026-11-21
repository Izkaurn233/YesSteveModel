package com.elfmcys.ysm.natives.legacy;

import com.elfmcys.ysm.buffer.NativeBuffer;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;

final class LegacyPayloadBuffer implements NativeBuffer {
    private final ByteBuffer data;
    private final NativeLegacyImportResult owner;

    static LegacyPayloadBuffer publicView(ByteBuffer data, NativeLegacyImportResult owner) {
        return new LegacyPayloadBuffer(data, owner);
    }

    private LegacyPayloadBuffer(ByteBuffer data, NativeLegacyImportResult owner) {
        this.data = data.slice();
        this.owner = owner;
    }

    @Override
    public ByteBuffer nio() {
        owner.get();
        return data.duplicate();
    }

    @Override
    public long ptr() {
        owner.get();
        return MemorySegment.ofBuffer(data).address();
    }

    @Override
    public int size() {
        owner.get();
        return data.capacity();
    }
}
