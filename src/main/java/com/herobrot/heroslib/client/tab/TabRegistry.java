package com.herobrot.heroslib.client.tab;

import com.herobrot.heroslib.HerosLib;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

import java.util.*;

public class TabRegistry {
    private static final Map<Class<? extends Screen>, List<TabDefinition>> TABS = new HashMap<>();

    public static void registerTab(Class<? extends Screen> parentClass, TabDefinition tab) {
        List<TabDefinition> tabs = TABS.computeIfAbsent(parentClass, k -> new ArrayList<>());
        boolean conflict = tabs.stream().anyMatch(existingTab ->
                existingTab.id().equals(tab.id()) && existingTab.targetScreen().equals(tab.targetScreen())
        );
        if (conflict) {
            HerosLib.LOGGER.warn("[HerosLib]: Conflicto de pestaña detectada. La pestaña con ID '{}' para '{}' ya ha sido registrada por otro mod.",
                    tab.id(), tab.targetScreen().getSimpleName());
            return;
        }
        tabs.add(tab);
        tabs.sort(Comparator.comparingInt(TabDefinition::priority));
    }

    public static void registerInventoryTab(TabDefinition tab) {
        registerTab(InventoryScreen.class, tab);
    }

    public static List<TabDefinition> getTabsFor(Class<? extends Screen> screenClass) {
        return TABS.getOrDefault(screenClass, Collections.emptyList());
    }
}