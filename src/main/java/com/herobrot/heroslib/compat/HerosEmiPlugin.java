package com.herobrot.heroslib.compat;

import com.herobrot.heroslib.client.event.ITabbedScreen;
import com.herobrot.heroslib.client.tab.TabRegistry;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

@EmiEntrypoint
public class HerosEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        registry.addGenericExclusionArea((screen, consumer) -> {
            int guiLeft = 0;
            int guiTop = 0;
            int tabCount = 0;

            if (screen instanceof InventoryScreen inv) {
                guiLeft = inv.getGuiLeft();
                guiTop = inv.getGuiTop();
                tabCount = TabRegistry.getTabsFor(InventoryScreen.class).size();
            } else if (screen instanceof ITabbedScreen tabbed) {
                Class<? extends Screen> parentClass = tabbed.getParentScreenClass();
                if (parentClass != null) {
                    guiLeft = tabbed.getGuiLeft();
                    guiTop = tabbed.getGuiTop();
                    tabCount = TabRegistry.getTabsFor(parentClass).size();
                }
            }

            if (tabCount > 0)
                consumer.accept(new Bounds(guiLeft, guiTop - 32, tabCount * 30, 32));
        });
    }
}