package com.elfmcys.ysm.client.gui.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

public final class ConfigCheckBox {
    private ConfigCheckBox() {}

    public static Checkbox create(int x, int y, String key, ForgeConfigSpec.BooleanValue value) {
        return Checkbox.builder(Component.translatable("gui.yes_steve_model.config." + key), Minecraft.getInstance().font)
            .pos(x, y)
            .maxWidth(400)
            .selected(value.get())
            .onValueChange((checkbox, selected) -> value.set(selected))
            .build();
    }
}
