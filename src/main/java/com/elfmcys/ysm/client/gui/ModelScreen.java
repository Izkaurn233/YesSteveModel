package com.elfmcys.ysm.client.gui;

import com.elfmcys.ysm.util.ResourceDiagnostics;
import java.lang.ref.WeakReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/** A model browser and its child screens cannot outlive their client world. */
public abstract class ModelScreen extends Screen {
    private static @Nullable Lifetime active;
    private static WeakReference<ClientLevel> unloadedLevel = new WeakReference<>(null);
    private final @Nullable Lifetime lifetime;

    protected ModelScreen(Component title) {
        super(title);
        lifetime = new Lifetime(Minecraft.getInstance().level);
    }

    protected ModelScreen(Component title, @Nullable ModelScreen parent) {
        super(title);
        lifetime = parent == null ? null : parent.lifetime;
    }

    public final boolean hasCurrentWorld() {
        return lifetime == null || lifetime.isCurrent();
    }

    public final void showScreen(@Nullable Screen screen) {
        if (!hasCurrentWorld())
            return;
        // Vanilla dialogs must belong to the same flow even though they do not extend ModelScreen.
        if (lifetime != null) {
            if (screen == null)
                lifetime.retire();
            else
                lifetime.displayed = new WeakReference<>(screen);
        }
        var minecraft = Minecraft.getInstance();
        try {
            minecraft.gui.setScreen(screen);
        } finally {
            // A cancelled opening keeps the source in the same world-bound flow.
            if (lifetime != null && lifetime.isCurrent() && minecraft.gui.screen() == this)
                lifetime.displayed = new WeakReference<>(this);
        }
    }

    @Override
    protected void clearWidgets() {
        // clearWidgets does not clear the focused child, which may own a preview entity.
        setFocused(null);
        super.clearWidgets();
    }

    @Override
    public void removed() {
        clearWidgets();
        if (lifetime != null && lifetime.displayed.get() == this)
            lifetime.retire();
        super.removed();
    }

    public static boolean allowOpening(@Nullable Screen target) {
        if (!(target instanceof ModelScreen screen) || screen.lifetime == null)
            return true;
        if (!screen.hasCurrentWorld()) {
            ResourceDiagnostics.log("model GUI opening rejected: screen={} reason=world-ended-or-unavailable",
                screen.getClass().getSimpleName());
            return false;
        }
        if (active != null && active != screen.lifetime)
            active.retire();
        active = screen.lifetime;
        active.displayed = new WeakReference<>(screen);
        return true;
    }

    public static void unload(ClientLevel level) {
        // The level hook runs before Minecraft replaces or clears its level field.
        unloadedLevel = new WeakReference<>(level);
        if (ResourceDiagnostics.isEnabled())
            ResourceDiagnostics.track(ResourceDiagnostics.Kind.CLIENT_LEVEL, level,
                "client level " + level.dimension().identifier());
        var closing = active;
        if (closing == null || closing.level.get() != level)
            return;
        var screen = closing.displayed.get();
        closing.retire();
        if (screen != null && Minecraft.getInstance().gui.screen() == screen)
            Minecraft.getInstance().gui.setScreen(null);
        ResourceDiagnostics.log("model GUI world retired: world={}", System.identityHashCode(level));
    }

    private static final class Lifetime {
        private final WeakReference<ClientLevel> level;
        private WeakReference<Screen> displayed = new WeakReference<>(null);
        private boolean retired;

        private Lifetime(@Nullable ClientLevel level) {
            this.level = new WeakReference<>(level);
        }

        private boolean isCurrent() {
            var current = Minecraft.getInstance().level;
            return !retired && current != null && level.get() == current && unloadedLevel.get() != current;
        }

        private void retire() {
            retired = true;
            level.clear();
            displayed.clear();
            if (active == this)
                active = null;
        }
    }
}
