package com.elfmcys.ysm.platform.capability;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The subset of Forge {@code LazyOptional} that YSM call sites use, so {@code entity.getCapability(KEY)} chains keep
 * compiling unchanged. Creation is deferred to the first terminal call, like {@code LazyOptional.of(supplier)}.
 * <p>
 * PORT(26.3-fabric): there is no invalidation; Forge's {@code invalidateCaps/reviveCaps} have no counterpart.
 */
public final class CapOptional<T> {
    private static final CapOptional<?> EMPTY = new CapOptional<>(null);

    private final @Nullable Supplier<T> supplier;
    private @Nullable T value;

    private CapOptional(@Nullable Supplier<T> supplier) {
        this.supplier = supplier;
    }

    static <T> CapOptional<T> of(Supplier<T> supplier) {
        return new CapOptional<>(supplier);
    }

    /** Present only when {@code value} is non-null, like {@code LazyOptional.of(() -> instance)} guarded by a null check. */
    static <T> CapOptional<T> ofNullable(@Nullable T value) {
        return value == null ? empty() : new CapOptional<>(() -> value);
    }

    @SuppressWarnings("unchecked")
    public static <T> CapOptional<T> empty() {
        return (CapOptional<T>)EMPTY;
    }

    private @Nullable T resolveValue() {
        if (value == null && supplier != null) {
            value = supplier.get();
        }
        return value;
    }

    public boolean isPresent() {
        return supplier != null;
    }

    public void ifPresent(Consumer<? super T> consumer) {
        T resolved = resolveValue();
        if (resolved != null) {
            consumer.accept(resolved);
        }
    }

    public <U> CapOptional<U> map(Function<? super T, ? extends U> mapper) {
        if (supplier == null) {
            return empty();
        }
        return of(() -> mapper.apply(resolveValue()));
    }

    public Optional<T> resolve() {
        return Optional.ofNullable(resolveValue());
    }

    public T orElse(T other) {
        T resolved = resolveValue();
        return resolved != null ? resolved : other;
    }

    public T orElseGet(Supplier<? extends T> other) {
        T resolved = resolveValue();
        return resolved != null ? resolved : other.get();
    }
}
