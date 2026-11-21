package com.elfmcys.ysm.client.gui.button;

import java.text.DecimalFormat;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

public abstract class RangeSlider extends AbstractSliderButton {
    private final Component prefix;
    private final Component suffix;
    private final double min;
    private final double max;
    private final double step;
    private final DecimalFormat format;

    protected RangeSlider(int x, int y, int width, int height, Component prefix, Component suffix,
        double min, double max, double current, double step) {
        super(x, y, width, height, Component.empty(), 0);
        this.prefix = prefix;
        this.suffix = suffix;
        this.min = min;
        this.max = max;
        this.step = Math.abs(step);
        format = new DecimalFormat("0");
        if (this.step > 0) {
            format.setMinimumFractionDigits(Math.max(0,
                java.math.BigDecimal.valueOf(this.step).stripTrailingZeros().scale()));
        }
        this.value = normalize(current);
        updateMessage();
    }

    protected final double getValue() {
        return min + value * (max - min);
    }

    protected String getValueString() {
        return format.format(getValue());
    }

    public final void setDisplayedValue(double value) {
        this.value = normalize(value);
        updateMessage();
    }

    @Override
    protected final void setValue(double progress) {
        super.setValue(normalize(min + progress * (max - min)));
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (canChangeValue && step > 0 && (event.isLeft() || event.isRight())) {
            double direction = event.isLeft() ? -1 : 1;
            super.setValue(normalize(getValue() + direction * Math.signum(max - min) * step));
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    protected final void updateMessage() {
        setMessage(Component.empty().append(prefix).append(getValueString()).append(suffix));
    }

    private double normalize(double value) {
        if (min == max) {
            return 0;
        }
        double low = Math.min(min, max);
        double high = Math.max(min, max);
        double clamped = Math.clamp(value, low, high);
        double snapped = step == 0 ? clamped : Math.clamp(Math.round(clamped / step) * step, low, high);
        return (snapped - min) / (max - min);
    }
}
