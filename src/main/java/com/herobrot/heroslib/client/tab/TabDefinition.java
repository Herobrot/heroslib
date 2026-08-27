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
        @Nullable BooleanSupplier allowKeySwitch,
        @Nullable BooleanSupplier showCondition
) {

    private static Runnable wrapSupplier(Supplier<Screen> screenSupplier) {
        return () -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) mc.setScreen(screenSupplier.get());
        };
    }

    public boolean shouldShow(Minecraft client) {
        return showCondition == null || showCondition.getAsBoolean();
    }

    public boolean canSwitchByKey() {
        return allowKeySwitch != null && allowKeySwitch.getAsBoolean();
    }

    public static Builder builder(ResourceLocation id, Component tooltip, Class<? extends Screen> targetScreen, int priority) {
        return new Builder(id, tooltip, targetScreen, priority);
    }

    public static class Builder {
        private final ResourceLocation id;
        private final Component tooltip;
        private final Class<? extends Screen> targetScreen;
        private final int priority;

        private ItemStack icon = null;
        private ResourceLocation iconTexture = null;
        private Runnable onTabClick = null;
        private boolean needsMouseFix = false;
        private KeyMapping keyMapping = null;
        private BooleanSupplier allowKeySwitch = null;
        private BooleanSupplier showCondition = null;

        public Builder(ResourceLocation id, Component tooltip, Class<? extends Screen> targetScreen, int priority) {
            this.id = id;
            this.tooltip = tooltip;
            this.targetScreen = targetScreen;
            this.priority = priority;
        }

        public Builder icon(ItemStack icon) {
            this.icon = icon;
            return this;
        }

        public Builder icon(ResourceLocation iconTexture) {
            this.iconTexture = iconTexture;
            return this;
        }

        public Builder onClick(Runnable onTabClick) {
            this.onTabClick = onTabClick;
            return this;
        }

        public Builder onOpen(Supplier<Screen> screenSupplier) {
            this.onTabClick = wrapSupplier(screenSupplier);
            return this;
        }

        public Builder needsMouseFix(boolean needsMouseFix) {
            this.needsMouseFix = needsMouseFix;
            return this;
        }

        public Builder keyMapping(KeyMapping keyMapping) {
            this.keyMapping = keyMapping;
            return this;
        }

        public Builder allowKeySwitch(BooleanSupplier allowKeySwitch) {
            this.allowKeySwitch = allowKeySwitch;
            return this;
        }

        public Builder showCondition(BooleanSupplier showCondition) {
            this.showCondition = showCondition;
            return this;
        }

        public TabDefinition build() {
            return new TabDefinition(
                    id, icon, iconTexture, null, null, tooltip, targetScreen,
                    onTabClick, priority, needsMouseFix, keyMapping, allowKeySwitch, showCondition
            );
        }
    }
}