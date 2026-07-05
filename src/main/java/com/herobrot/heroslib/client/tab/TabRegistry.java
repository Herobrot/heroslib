package com.herobrot.heroslib.client.tab;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

public class TabRegistry {
    private static final List<TabDefinition> INVENTORY_TABS = new ArrayList<>();
    private static final Map<Class<? extends Screen>, List<TabDefinition>> OTHER_TABS = new HashMap<>();

    static {
        registerInventoryTab(new TabDefinition(
                ResourceLocation.withDefaultNamespace("inventory"),
                new ItemStack(Items.CHEST),
                Component.translatable("gui.heroslib.tab.inventory"),
                InventoryScreen.class,
                null,
                0,
                false
        ));
    }

    public static void registerInventoryTab(TabDefinition tab) {
        if (INVENTORY_TABS.stream().noneMatch(t -> t.id().equals(tab.id()))) {
            INVENTORY_TABS.add(tab);
            INVENTORY_TABS.sort(Comparator.comparingInt(TabDefinition::priority));
        }
    }

    @SuppressWarnings("unused")
    public static void registerOtherTab(TabDefinition tab, Class<? extends Screen> parentClass) {
        OTHER_TABS.computeIfAbsent(parentClass, k -> new ArrayList<>()).add(tab);
        OTHER_TABS.get(parentClass).sort(Comparator.comparingInt(TabDefinition::priority));
    }

    public static List<TabDefinition> getInventoryTabs() {
        return Collections.unmodifiableList(INVENTORY_TABS);
    }

    public static List<TabDefinition> getTabsFor(Class<? extends Screen> screenClass) {
        return OTHER_TABS.getOrDefault(screenClass, Collections.emptyList());
    }
}