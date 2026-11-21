package com.elfmcys.ysm.model.catalog.source;

import com.elfmcys.ysm.platform.Platform;
import com.elfmcys.ysm.AssetPaths;
import com.elfmcys.ysm.model.ModelRuntime;

import java.nio.file.Path;
import java.util.List;

public final class ModelCatalogSources {
    private ModelCatalogSources() {}

    public static List<ModelCatalogSource> sources() {
        return sources(ModelRuntime.paths());
    }

    public static List<ModelCatalogSource> reloadableSources() {
        return reloadableSources(ModelRuntime.paths());
    }

    public static List<ModelCatalogSource> sources(AssetPaths paths) {
        var reloadable = reloadableSources(paths);
        var sources = new java.util.ArrayList<ModelCatalogSource>();
        sources.add(builtin(paths));
        sources.addAll(reloadable);
        return List.copyOf(sources);
    }

    public static ModelCatalogSource builtin(AssetPaths paths) {
        return new ModelCatalogSource(CatalogRootKind.BUILTIN, paths.builtinModelsResource(), false);
    }

    public static List<ModelCatalogSource> reloadableSources(AssetPaths paths) {
        var sources = new java.util.ArrayList<ModelCatalogSource>();
        paths.global().ifPresent(global -> {
            sources.add(new ModelCatalogSource(CatalogRoot.GLOBAL_CUSTOM, global.customRoot(), false));
            sources.add(new ModelCatalogSource(CatalogRoot.GLOBAL_AUTH, global.authRoot(), false));
        });
        sources.add(new ModelCatalogSource(CatalogRoot.LOCAL_CUSTOM, paths.customModelsRoot(), true));
        sources.add(new ModelCatalogSource(CatalogRoot.LOCAL_AUTH, paths.authModelsRoot(),
            Platform.isDedicatedServer()));
        return List.copyOf(sources);
    }

    public static Path customPath() {
        return ModelRuntime.paths().defaultCustomRoot();
    }
}
