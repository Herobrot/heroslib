package com.herobrot.heroslib.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import sfiomn.legendarytabs.api.tabs_menu.TabBase;
import sfiomn.legendarytabs.api.tabs_menu.TabsMenu;

import java.util.List;

public class LegendaryTabsCompat {

    public static int getActiveTabBarWidth(Class<?> screenClass) {
        TabsMenu.ScreenInfo info = TabsMenu.getScreenInfo(screenClass);
        if (info == null || info.tabs == null) return 0;
        Player player = Minecraft.getInstance().player;
        if (player == null) return 0;
        int count = 0;
        for (List<TabBase> tabList : info.tabs.values())
            for (TabBase tab : tabList) if (tab.isEnabled(player)) count++;

        return (count * (TabBase.TAB_WIDTH + 1)) + 3;
    }

    public static boolean isInventoryTabActive(Class<?> screenClass) {
        TabsMenu.ScreenInfo info = TabsMenu.getScreenInfo(screenClass);
        if (info == null || info.tabs == null) return false;
        Player player = Minecraft.getInstance().player;
        if (player == null) return false;
        for (List<TabBase> tabList : info.tabs.values())
            for (TabBase tab : tabList)
                if (tab.isEnabled(player) && tab.getId().equals("inventory"))
                    return true;
        return false;
    }
}