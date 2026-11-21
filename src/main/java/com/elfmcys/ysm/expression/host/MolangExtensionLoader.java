package com.elfmcys.ysm.expression.host;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.api.molang.DeclarationSchema;
import com.elfmcys.ysm.platform.Platform;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

/**
 * Called by the client startup owner before it constructs any target factory.
 *
 * <p>Requires: Fabric Loader mod discovery and the non-null host-availability snapshot are complete. This loader
 * freezes the process collector once; it is not a per-world reload or a repeatable service factory.
 *
 * <p>Ensures: normal return exposes one frozen declaration host after every admitted class reaches
 * its outcome. Rejected classes contribute diagnostics, not placeholders. Admission does not certify
 * provider thread safety or construct installation-local components.
 *
 * <p>Execution: synchronous metadata reads, checker execution, binding, and class initialization on
 * the startup caller. Discovery itself does not initialize implementation classes.
 *
 * <p>Seam: implements [extension qualification and freeze](docs/architecture/integration/overview.md#扩展接纳与声明发布).
 */
public final class MolangExtensionLoader {
    private MolangExtensionLoader() {}

    /**
     * Freezes the process declaration collector and returns its non-null host.
     *
     * @throws IllegalStateException if collection has already been sealed; failure does not reopen collection
     */
    public static ExpressionHost createHost(HostAvailability availability) {
        var mods = FabricLoader.getInstance().getAllMods();
        var admitted = com.elfmcys.ysm.api.internal.MolangDeclarationBootstrap.freeze(
            Objects.requireNonNull(availability, "availability"),
            discover(mods, MolangExtensionLoader.class.getClassLoader()),
            new MolangExtensions.Qualification() {
                public boolean dependency(String id, String range) {
                    return Platform.modVersion(id).filter(version -> range.isEmpty()
                        || MolangExtensions.versionMatches(version, range)).isPresent();
                }

                public boolean checker(MolangExtensions.Source source,
                    com.elfmcys.ysm.api.molang.DeclarationSchema schema) {
                    if (schema.builtin())
                        return schema.dependencies().stream()
                            .allMatch(dependency -> java.util.Arrays.stream(OptionalCapability.values())
                                .filter(capability -> capability.modId().equals(dependency.modId()))
                                .allMatch(availability::supports));
                    if (schema.manual())
                        return true;
                    try {
                        var result = com.elfmcys.ysm.api.internal.CompatibilityQualification.check(schema.owner(),
                            source.loader());
                        result.issues().forEach(issue -> YesSteveModel.LOGGER.warn("Molang checker: {}", issue));
                        return result.compatible();
                    } catch (ReflectiveOperationException | RuntimeException | LinkageError failure) {
                        throw new IllegalArgumentException("cannot qualify generated checker", failure);
                    }
                }
            });
        admitted.diagnostics()
            .forEach(message -> YesSteveModel.LOGGER.error("Molang extension admission: {}", message));
        return admitted.host();
    }

    /** Reads generated declaration resources from their owning mod roots without loading provider classes. */
    public static List<MolangExtensions.Source> discover(Collection<? extends ModContainer> mods, ClassLoader loader) {
        var sources = new ArrayList<MolangExtensions.Source>();
        for (var mod : mods) {
            var id = mod.getMetadata().getId();
            if (YesSteveModel.MOD_ID.equals(id)) {
                continue;
            }
            for (var root : mod.getRootPaths()) {
                var directory = root.resolve(DeclarationSchema.RESOURCE_ROOT);
                if (!Files.isDirectory(directory)) {
                    continue;
                }
                var declarations = new ArrayList<MolangExtensions.DeclarationRef>();
                try (var paths = Files.list(directory)) {
                    for (var resource : paths.filter(Files::isRegularFile).sorted().toList()) {
                        readDeclaration(id, resource, declarations);
                    }
                } catch (IOException | RuntimeException failure) {
                    YesSteveModel.LOGGER.error("Failed to enumerate Molang declarations for {}", id, failure);
                }
                if (!declarations.isEmpty()) {
                    sources.add(new MolangExtensions.Source(id + ":" + root, Set.of(id), declarations, loader, root));
                }
            }
        }
        return List.copyOf(sources);
    }

    private static void readDeclaration(String id, Path resource, List<MolangExtensions.DeclarationRef> declarations) {
        var fileName = resource.getFileName().toString();
        if (!fileName.endsWith(".bin")) {
            return;
        }
        try (var input = Files.newInputStream(resource)) {
            var data = input.readNBytes(1_048_577);
            if (data.length > 1_048_576) {
                throw new IOException("source resource exceeds admission limit");
            }
            var schema = DeclarationSchema.decode(new ByteArrayInputStream(data));
            if (!fileName.equals(schema.owner() + ".bin")) {
                throw new IOException("declaration path does not match its provider");
            }
            declarations.add(new MolangExtensions.DeclarationRef(schema.owner(), schema.namespace(), schema.modId()));
        } catch (IOException | RuntimeException failure) {
            YesSteveModel.LOGGER.error("Invalid Molang declaration {} from {}", fileName, id, failure);
        }
    }
}
