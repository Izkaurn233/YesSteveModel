package com.elfmcys.ysm.api.model.event;

import com.elfmcys.ysm.api.event.Event;
import com.elfmcys.ysm.api.model.ModelKind;
import com.elfmcys.ysm.geckolib3.geo.render.built.GeoLocator;

import java.util.function.Function;

public class RegisterModelLocatorEvent implements Event {
    private final ModelKind kind;
    private final Function<String, GeoLocator> registerFunction;

    public RegisterModelLocatorEvent(ModelKind kind, Function<String, GeoLocator> registerFunction) {
        this.kind = kind;
        this.registerFunction = registerFunction;
    }

    public ModelKind kind() {
        return kind;
    }

    public GeoLocator register(String locatorName) {
        return registerFunction.apply(locatorName);
    }
}
