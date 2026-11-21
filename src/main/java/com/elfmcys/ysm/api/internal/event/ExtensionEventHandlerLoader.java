package com.elfmcys.ysm.api.internal.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.api.annotation.YsmEventHandler;
import com.elfmcys.ysm.api.event.EventManager;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

/**
 * Internal discovery and registration bridge for {@link YsmEventHandler}.
 *
 * <p>Discovery (PORT(26.3-fabric)): Forge found {@code @YsmEventHandler} types in its annotation scan data without
 * loading them. Fabric has no such scan, so an extension lists its handler binary names in {@code fabric.mod.json}:
 * <pre>{@code
 * "custom": { "ysm:event_handlers": ["com.example.MyYsmHandler"] }
 * }</pre>
 * Reading metadata loads no class, so the generated checker still runs before any handler class is initialized.
 * The {@code @YsmEventHandler} annotation stays on the class and still drives the annotation processor's checker.
 *
 * <p>Execution: Forge registered in {@code FMLLoadCompleteEvent}; Fabric has no load-complete phase, so
 * {@link #registerAll(EventManager)} runs once from the main entrypoint. That is earlier than Forge, so handlers
 * are present before client setup posts the locator events.
 *
 * <p>Failure: qualification failures are isolated per handler. Registration exceptions are logged,
 * but this bridge currently does not undo any listeners installed before register throws.
 *
 * <p>Seam: implements [optional handler admission](docs/architecture/integration/overview.md#失败隔离与声明寿命); see the
 * [partial-registration rollback gap](docs/status/known-issues/integration.md#可选-handler-注册回滚).
 */
public final class ExtensionEventHandlerLoader {
    /** {@code fabric.mod.json} custom key holding an array of {@code @YsmEventHandler} binary class names. */
    public static final String METADATA_KEY = "ysm:event_handlers";

    private ExtensionEventHandlerLoader() {}

    /** Discovers every declared handler and registers the qualified ones on {@code events}. Call once. */
    public static void registerAll(EventManager events) {
        List<String> handlerNames;
        try {
            handlerNames = discoverHandlerNames(FabricLoader.getInstance().getAllMods());
        } catch (RuntimeException | LinkageError exception) {
            YesSteveModel.LOGGER.error("Failed to discover @YsmEventHandler classes", exception);
            return;
        }

        ClassLoader loader = ExtensionEventHandlerLoader.class.getClassLoader();
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        for (String handlerName : handlerNames) {
            registerOne(events, handlerName, loader);
        }
    }

    /** Sorted, distinct handler names declared under {@link #METADATA_KEY}; malformed entries are logged and skipped. */
    static List<String> discoverHandlerNames(Collection<ModContainer> mods) {
        var names = new TreeSet<String>();
        for (ModContainer mod : mods) {
            var metadata = mod.getMetadata();
            if (!metadata.containsCustomValue(METADATA_KEY)) {
                continue;
            }
            CustomValue value = metadata.getCustomValue(METADATA_KEY);
            if (value.getType() != CustomValue.CvType.ARRAY) {
                YesSteveModel.LOGGER.error("Mod {}: custom value {} must be an array of class names", metadata.getId(),
                    METADATA_KEY);
                continue;
            }
            for (CustomValue entry : value.getAsArray()) {
                if (entry.getType() != CustomValue.CvType.STRING || entry.getAsString().isBlank()) {
                    YesSteveModel.LOGGER.error("Mod {}: ignoring non-string entry in {}", metadata.getId(),
                        METADATA_KEY);
                    continue;
                }
                names.add(entry.getAsString().trim());
            }
        }
        return new ArrayList<>(names);
    }

    private static void registerOne(EventManager events, String handlerName, ClassLoader loader) {
        ExtensionEventHandlerRegistration.Outcome outcome;
        try {
            outcome = ExtensionEventHandlerRegistration.prepare(handlerName, loader);
        } catch (ExtensionEventHandlerRegistration.Failure failure) {
            if (failure.stage() == ExtensionEventHandlerRegistration.Stage.CHECKER_MISSING) {
                YesSteveModel.LOGGER.error("Skipping @YsmEventHandler {} because checker {} is missing. "
                    + "Add the classifierless YSM JAR to annotationProcessor.", handlerName, failure.subject());
            } else {
                YesSteveModel.LOGGER.error("Skipping @YsmEventHandler {}: {} ({})", handlerName, failure.getMessage(),
                    failure.subject(), failure.getCause());
            }
            return;
        }

        if (!outcome.shouldRegister()) {
            YesSteveModel.LOGGER.warn("Skipping @YsmEventHandler {}: status={}, coverageComplete={}, issues={}",
                handlerName, outcome.status(), outcome.coverageComplete(), outcome.issues());
            return;
        }
        if (outcome.hasWarnings()) {
            YesSteveModel.LOGGER.warn(
                "Registering @YsmEventHandler {} with compatibility warnings: "
                    + "status={}, coverageComplete={}, issues={}",
                handlerName, outcome.status(), outcome.coverageComplete(), outcome.issues());
        }

        try {
            events.register(outcome.handler());
            YesSteveModel.LOGGER.info("Registered @YsmEventHandler {}", handlerName);
        } catch (RuntimeException | LinkageError exception) {
            YesSteveModel.LOGGER.error("Failed to register @YsmEventHandler {}; continuing without it", handlerName,
                exception);
        }
    }
}
