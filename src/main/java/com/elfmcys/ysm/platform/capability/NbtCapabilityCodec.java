package com.elfmcys.ysm.platform.capability;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Codecs for capabilities that keep their Forge-era {@code serializeNBT/deserializeNBT} methods.
 * <p>
 * PORT(26.3-fabric): the stored layout is Fabric's attachment NBT, not Forge's {@code ForgeCaps} compound. Worlds from
 * the Forge build are a different loader and are not migrated.
 */
public final class NbtCapabilityCodec {
    private static final String LIST_KEY = "entries";

    private NbtCapabilityCodec() {
    }

    public static <T> Codec<T> compound(Supplier<T> constructor, Function<T, CompoundTag> write,
        BiConsumer<T, CompoundTag> read) {
        return CompoundTag.CODEC.xmap(tag -> {
            T value = constructor.get();
            read.accept(value, tag);
            return value;
        }, write);
    }

    /** {@link ListTag} payloads are wrapped in a compound, since 26.3 exposes a codec for compounds only. */
    public static <T> Codec<T> list(Supplier<T> constructor, Function<T, ListTag> write, BiConsumer<T, ListTag> read) {
        return CompoundTag.CODEC.xmap(tag -> {
            T value = constructor.get();
            read.accept(value, tag.getListOrEmpty(LIST_KEY));
            return value;
        }, value -> {
            var tag = new CompoundTag();
            tag.put(LIST_KEY, write.apply(value));
            return tag;
        });
    }
}
