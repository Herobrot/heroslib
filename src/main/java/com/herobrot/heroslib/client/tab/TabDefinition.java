package com.herobrot.heroslib.client.tab;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.function.Supplier;

public record TabDefinition(ResourceLocation id, ItemStack icon, Component tooltip,
                            Class<? extends Screen> targetScreen,
                            Supplier<Screen> screenSupplier, int priority, boolean needsMouseFix) {

    public boolean shouldShow(Minecraft client) {
        return true;
    }
}