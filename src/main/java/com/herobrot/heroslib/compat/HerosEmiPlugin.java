package com.herobrot.heroslib.compat;

import com.herobrot.heroslib.api.HerosLibAPI;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;

@EmiEntrypoint
public class HerosEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        registry.addGenericExclusionArea((screen, consumer) -> {
            if (HerosLibAPI.isTabbedScreen(screen)) {
                int guiLeft = HerosLibAPI.getGuiLeft(screen);
                int guiTop = HerosLibAPI.getGuiTop(screen);
                int width = HerosLibAPI.getTabBarWidth(screen);
                if (width > 0)
                    consumer.accept(new Bounds(guiLeft, guiTop - HerosLibAPI.TAB_HEIGHT, width, HerosLibAPI.TAB_HEIGHT));
            }
        });
    }
}