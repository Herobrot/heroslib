package com.herobrot.heroslib.client.tab;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

public class TabRegistry {
    // UNIFICACIÓN: Un solo mapa para todas las pestañas, agrupadas por su pantalla padre
    private static final Map<Class<? extends Screen>, List<TabDefinition>> TABS = new HashMap<>();

    static {
        // La pestaña de vainilla se registra bajo la llave de InventoryScreen.class
        registerTab(InventoryScreen.class, new TabDefinition(
                ResourceLocation.withDefaultNamespace("inventory"),
                new ItemStack(Items.CHEST),
                Component.translatable("gui.heroslib.tab.inventory"),
                InventoryScreen.class,
                null,
                0,
                false
        ));
    }

    public static void registerTab(Class<? extends Screen> parentClass, TabDefinition tab) {
        TABS.computeIfAbsent(parentClass, k -> new ArrayList<>()).add(tab);
        TABS.get(parentClass).sort(Comparator.comparingInt(TabDefinition::priority));
    }

    public static void registerInventoryTab(TabDefinition tab) {
        registerTab(InventoryScreen.class, tab);
    }

    public static List<TabDefinition> getTabsFor(Class<? extends Screen> screenClass) {
        return TABS.getOrDefault(screenClass, Collections.emptyList());
    }
}