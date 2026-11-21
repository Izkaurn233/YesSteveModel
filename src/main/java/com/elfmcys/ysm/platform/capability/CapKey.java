package com.elfmcys.ysm.platform.capability;

import com.elfmcys.ysm.YesSteveModel;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * One YSM entity capability. Replaces the Forge {@code Capability} token, its {@code ICapabilityProvider} and the
 * {@code AttachCapabilitiesEvent} rule that decided which entities get it.
 * <ul>
 * <li><b>Attached</b> keys are backed by a Fabric data attachment. {@code eligible} reproduces the Forge attach rule
 * and is checked on access, because Fabric has no attach event. The instance is created lazily by {@code factory},
 * as the Forge providers did through {@code LazyOptional.of(this::createCapability)}. A persistent key is saved with
 * the entity through its codec.</li>
 * <li><b>Derived</b> keys own no storage. They resolve to an instance held by another capability, and are present only
 * while that instance exists, the way the Forge vehicle/projectile animatable providers returned
 * {@code LazyOptional.empty()} until initialised.</li>
 * </ul>
 * Attached keys must be created during mod initialisation (Fabric registers the attachment type in the constructor).
 *
 * @param <T> capability type
 */
public final class CapKey<T> {
    private final Identifier id;
    private final @Nullable AttachmentType<T> type;
    private final Predicate<Entity> eligible;
    private final @Nullable Function<Entity, T> factory;
    private final @Nullable Function<Entity, @Nullable T> resolver;

    private CapKey(Identifier id, @Nullable AttachmentType<T> type, Predicate<Entity> eligible,
        @Nullable Function<Entity, T> factory, @Nullable Function<Entity, @Nullable T> resolver) {
        this.id = id;
        this.type = type;
        this.eligible = eligible;
        this.factory = factory;
        this.resolver = resolver;
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, path);
    }

    /** Runtime-only capability (Forge {@code ICapabilityProvider}). */
    public static <T> CapKey<T> transientKey(String path, Predicate<Entity> eligible, Function<Entity, T> factory) {
        Identifier id = id(path);
        return new CapKey<>(id, AttachmentRegistry.create(id), eligible, factory, null);
    }

    /** Saved with the entity (Forge {@code ICapabilitySerializable}). */
    public static <T> CapKey<T> persistentKey(String path, Codec<T> codec, Predicate<Entity> eligible,
        Function<Entity, T> factory) {
        Identifier id = id(path);
        return new CapKey<>(id, AttachmentRegistry.create(id, builder -> builder.persistent(codec)), eligible, factory,
            null);
    }

    /** Capability whose instance lives inside another capability; {@code resolver} returns null while absent. */
    public static <T> CapKey<T> derivedKey(String path, Function<Entity, @Nullable T> resolver) {
        return new CapKey<>(id(path), null, entity -> true, null, resolver);
    }

    public Identifier id() {
        return id;
    }

    /** Whether Forge would have attached this capability to {@code entity}. */
    public boolean isEligible(Entity entity) {
        return eligible.test(entity);
    }

    public CapOptional<T> get(Entity entity) {
        if (resolver != null) {
            return CapOptional.ofNullable(resolver.apply(entity));
        }
        if (!eligible.test(entity)) {
            return CapOptional.empty();
        }
        AttachmentType<T> attachment = type;
        Function<Entity, T> create = factory;
        return CapOptional.of(() -> entity.getAttachedOrCreate(attachment, () -> create.apply(entity)));
    }

    /** The current instance without creating one, or null. */
    public @Nullable T peek(Entity entity) {
        if (resolver != null) {
            return resolver.apply(entity);
        }
        return entity.getAttached(type);
    }
}
