package com.herobrot.heroslib.api;

import com.herobrot.heroslib.HerosLib;
import com.herobrot.heroslib.client.event.ITabbedScreen;
import com.herobrot.heroslib.client.tab.TabDefinition;
import com.herobrot.heroslib.client.tab.TabRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

import java.util.List;

public class HerosLibAPI {

    private HerosLibAPI() {}

    // Standard dimensions used by the library
    public static final int TAB_WIDTH = 30;
    public static final int TAB_HEIGHT = 32;

    /**
     * Gets the X-coordinate (leftPos) of the screen managed by HerosLib.
     */
    public static int getGuiLeft(Screen screen) {
        return switch (screen) {
            case InventoryScreen inv -> inv.getGuiLeft();
            case ITabbedScreen tabbed -> tabbed.heroslib$getGuiLeft();
            default -> 0;
        };
    }

    /**
     * Gets the Y-coordinate (topPos) of the screen managed by HerosLib.
     */
    public static int getGuiTop(Screen screen) {
        return switch (screen) {
            case InventoryScreen inv -> inv.getGuiTop();
            case ITabbedScreen tabbed -> tabbed.heroslib$getGuiTop();
            default -> 0;
        };
    }

    /**
     * Returns the list of tabs registered by mods for a specific screen class.
     *
     * @param screenClass The screen class (e.g., InventoryScreen.class).
     * @return An immutable list of TabDefinitions, or an empty list if there are no tabs.
     */
    public static List<TabDefinition> getRegisteredTabs(Class<? extends Screen> screenClass) {
        return TabRegistry.getTabsFor(screenClass);
    }

    /**
     * Calculate the total width in pixels that the visible HerosLib tabs will occupy
     * on the provided screen.
     *
     * @param screen The currently open screen.
     * @return The width in pixels, or 0 if HerosLib does not draw tabs here.
     */
    public static int getTabBarWidth(Screen screen) {
        Class<? extends Screen> parentClass = resolveParentClass(screen);
        if (parentClass == null) return 0;

        List<TabDefinition> tabs = TabRegistry.getTabsFor(parentClass);
        if (tabs.isEmpty()) return 0;

        Minecraft mc = Minecraft.getInstance();
        int visibleCount = 0;
        for (TabDefinition tab : tabs) if (tab.shouldShow(mc)) visibleCount++;
        return visibleCount * TAB_WIDTH;
    }

    /**
     * Check if HerosLib is actively managing the current screen.
     * (that is, if it has tabs registered and ready to inject).
     *
     * @param screen The screen to be evaluated.
     * @return true if HerosLib will inject tabs into this screen.
     */
    public static boolean isTabbedScreen(Screen screen) {
        Class<? extends Screen> parentClass = resolveParentClass(screen);
        return parentClass != null && !TabRegistry.getTabsFor(parentClass).isEmpty();
    }

    /**
     * It resolves the parent class that HerosLib uses to group the tabs of a given screen.
     * If the screen is the native inventory, it returns InventoryScreen.class.
     * If it implements ITabbedScreen, it returns the getParentScreenClass().
     * If it is not managed, it returns null.
     *
     * @param screen The screen to be evaluated.
     * @return The grouping class, or null.
     */
    public static Class<? extends Screen> resolveParentClass(Screen screen) {
        return switch (screen) {
            case InventoryScreen inventoryScreen -> InventoryScreen.class;
            case ITabbedScreen tabbedScreen -> tabbedScreen.heroslib$getParentScreenClass();
            default -> null;
        };
    }

    /**
     * Allows other mods to know if the LegendaryTabs compatibility layer is active.
     *
     * @return true if LegendaryTabs is installed and loaded.
     */
    public static boolean isLegendaryTabsLoaded() {
        return HerosLib.isLegendaryTabsLoaded;
    }
}