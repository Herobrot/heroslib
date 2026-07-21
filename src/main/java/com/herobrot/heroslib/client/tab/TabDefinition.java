package com.herobrot.heroslib.client.tab;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public record TabDefinition(ResourceLocation id, ItemStack icon, Component tooltip,
                            Class<? extends Screen> targetScreen,
                            Supplier<Screen> screenSupplier, int priority, boolean needsMouseFix,
                            @Nullable KeyMapping keyMapping,
                            @Nullable BooleanSupplier allowKeySwitch) {

    public TabDefinition(ResourceLocation id, ItemStack icon, Component tooltip,
                         Class<? extends Screen> targetScreen,
                         Supplier<Screen> screenSupplier, int priority, boolean needsMouseFix) {
        this(id, icon, tooltip, targetScreen, screenSupplier, priority, needsMouseFix, null, null);
    }

    public TabDefinition(ResourceLocation id, ItemStack icon, Component tooltip,
                         Class<? extends Screen> targetScreen,
                         Supplier<Screen> screenSupplier, int priority, boolean needsMouseFix,
                         @Nullable KeyMapping keyMapping) {
        this(id, icon, tooltip, targetScreen, screenSupplier, priority, needsMouseFix, keyMapping, null);
    }

    public boolean shouldShow(Minecraft client) {
        return true;
    }

    public boolean canSwitchByKey() {
        return allowKeySwitch != null && allowKeySwitch.getAsBoolean();
    }
}