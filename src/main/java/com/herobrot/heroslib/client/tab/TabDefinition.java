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

public record TabDefinition(
        ResourceLocation id,
        @Nullable ItemStack icon,
        @Nullable ResourceLocation iconTexture,
        @Nullable ResourceLocation bgSelected,
        @Nullable ResourceLocation bgUnselected,
        Component tooltip,
        Class<? extends Screen> targetScreen,
        Runnable onTabClick,
        int priority,
        boolean needsMouseFix,
        @Nullable KeyMapping keyMapping,
        @Nullable BooleanSupplier allowKeySwitch
) {
    // --- Constructores para iconos de Items ---
    // Cliente
    public TabDefinition(ResourceLocation id, ItemStack icon, Component tooltip,
                         Class<? extends Screen> targetScreen,
                         Supplier<Screen> screenSupplier, int priority, boolean needsMouseFix) {
        this(id, icon, null, null, null, tooltip, targetScreen,
                wrapSupplier(screenSupplier), priority, needsMouseFix, null, null);
    }

    // Cliente + Atajo de teclado
    public TabDefinition(ResourceLocation id, ItemStack icon, Component tooltip,
                         Class<? extends Screen> targetScreen,
                         Supplier<Screen> screenSupplier, int priority, boolean needsMouseFix,
                         @Nullable KeyMapping keyMapping) {
        this(id, icon, null, null, null, tooltip, targetScreen,
                wrapSupplier(screenSupplier), priority, needsMouseFix, keyMapping, null);
    }

    // Cliente + Atajo de teclado + Lógica de intercambio
    public TabDefinition(ResourceLocation id, ItemStack icon, Component tooltip,
                         Class<? extends Screen> targetScreen,
                         Supplier<Screen> screenSupplier, int priority, boolean needsMouseFix,
                         @Nullable KeyMapping keyMapping, @Nullable BooleanSupplier allowKeySwitch) {
        this(id, icon, null, null, null, tooltip, targetScreen,
                wrapSupplier(screenSupplier), priority, needsMouseFix, keyMapping, allowKeySwitch);
    }

    // --- Constructores para iconos de texturas ---
    // Cliente
    public TabDefinition(ResourceLocation id, ResourceLocation iconTexture, Component tooltip,
                         Class<? extends Screen> targetScreen,
                         Supplier<Screen> screenSupplier, int priority, boolean needsMouseFix) {
        this(id, null, iconTexture, null, null, tooltip, targetScreen,
                wrapSupplier(screenSupplier), priority, needsMouseFix, null, null);
    }

    // Cliente + Atajo de teclado
    public TabDefinition(ResourceLocation id, ResourceLocation iconTexture, Component tooltip,
                         Class<? extends Screen> targetScreen,
                         Supplier<Screen> screenSupplier, int priority, boolean needsMouseFix,
                         @Nullable KeyMapping keyMapping) {
        this(id, null, iconTexture, null, null, tooltip, targetScreen,
                wrapSupplier(screenSupplier), priority, needsMouseFix, keyMapping, null);
    }

    // Cliente + Atajo de teclado + Lógica de intercambio
    public TabDefinition(ResourceLocation id, ResourceLocation iconTexture, Component tooltip,
                         Class<? extends Screen> targetScreen,
                         Supplier<Screen> screenSupplier, int priority, boolean needsMouseFix,
                         @Nullable KeyMapping keyMapping, @Nullable BooleanSupplier allowKeySwitch) {
        this(id, null, iconTexture, null, null, tooltip, targetScreen,
                wrapSupplier(screenSupplier), priority, needsMouseFix, keyMapping, allowKeySwitch);
    }

    private static Runnable wrapSupplier(Supplier<Screen> screenSupplier) {
        return () -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) mc.setScreen(screenSupplier.get());
        };
    }

    public boolean shouldShow(Minecraft client) { return true; }

    public boolean canSwitchByKey() {
        return allowKeySwitch != null && allowKeySwitch.getAsBoolean();
    }
}