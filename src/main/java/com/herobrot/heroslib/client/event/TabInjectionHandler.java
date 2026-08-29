package com.herobrot.heroslib.client.event;

import com.herobrot.heroslib.HerosLib;
import com.herobrot.heroslib.client.tab.TabDefinition;
import com.herobrot.heroslib.client.tab.TabRegistry;
import com.herobrot.heroslib.client.widget.TabButtonWidget;
import com.herobrot.heroslib.compat.LegendaryTabsCompat;
import com.herobrot.heroslib.mixin.MouseHandlerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@EventBusSubscriber(modid = HerosLib.MODID, value = Dist.CLIENT)
public class TabInjectionHandler {
    private static boolean expectingTabChange = false;
    private static double savedMouseX = 0;
    private static double savedMouseY = 0;

    private record ScreenTabContext(int guiLeft, int guiTop, Class<? extends Screen> parentClass,
                                    List<TabDefinition> tabs) {}

    private static ScreenTabContext resolveContext(Screen screen) {
        if (screen instanceof CreativeModeInventoryScreen) return null;
        int guiLeft;
        int guiTop;
        Class<? extends Screen> parentClass;

        if (screen instanceof InventoryScreen containerScreen) {
            guiLeft = containerScreen.getGuiLeft();
            guiTop = containerScreen.getGuiTop();
            parentClass = InventoryScreen.class;
        } else if (screen instanceof ITabbedScreen tabbedScreen) {
            guiLeft = tabbedScreen.heroslib$getGuiLeft();
            guiTop = tabbedScreen.heroslib$getGuiTop();
            parentClass = tabbedScreen.heroslib$getParentScreenClass();
            if (parentClass == null) return null;
        } else
            return null;

        List<TabDefinition> tabs = TabRegistry.getTabsFor(parentClass);
        return tabs.isEmpty() ? null : new ScreenTabContext(guiLeft, guiTop, parentClass, tabs);
    }

    /**
     * Calcula cuánto debemos desplazarnos a la derecha si LegendaryTabs está activo en esta pantalla.
     */
    private static int getLegendaryTabsOffset(Class<?> parentClass) {
        if (!HerosLib.isLegendaryTabsLoaded) return 0;
        try {
            return LegendaryTabsCompat.getActiveTabBarWidth(parentClass);
        } catch (Throwable e) {
            HerosLib.LOGGER.error("[HerosLib]: Error al calcular el offset de LegendaryTabs", e);
            return 0;
        }
    }

    private static boolean shouldSkipHomeTab(Class<?> parentClass) {
        if (!HerosLib.isLegendaryTabsLoaded) return false;
        try {
            return LegendaryTabsCompat.isInventoryTabActive(parentClass);
        } catch (Throwable e) {
            HerosLib.LOGGER.error("[HerosLib]: Error al comprobar la pestaña de inventario de LegendaryTabs", e);
            return false;
        }
    }

    @SubscribeEvent
    public static void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        Screen screen = event.getScreen();
        ScreenTabContext ctx = resolveContext(screen);
        if (ctx == null) return;
        GuiGraphics graphics = event.getGuiGraphics();
        int offset = getLegendaryTabsOffset(ctx.parentClass());
        boolean skipHomeTab = shouldSkipHomeTab(ctx.parentClass());
        int xPos = ctx.guiLeft() + offset;
        int topPos = ctx.guiTop() - 26;
        boolean isFirstTab = offset == 0;
        for (TabDefinition tab : ctx.tabs()) {
            if (!tab.shouldShow(Minecraft.getInstance())) continue;
            if (skipHomeTab && tab.targetScreen() == ctx.parentClass()) continue;
            boolean isSelected = tab.targetScreen().isAssignableFrom(screen.getClass());
            int tabY = isSelected ? topPos - 2 : topPos;
            if (!isSelected)
                TabButtonWidget.drawBackgroundStatic(graphics, xPos, topPos, isFirstTab, false);

            int finalXPos = xPos;
            screen.children().stream()
                    .filter(child -> child instanceof TabButtonWidget btn && btn.getTab() == tab)
                    .findFirst()
                    .ifPresent(btn -> ((TabButtonWidget) btn).setPosition(finalXPos, tabY));

            xPos += 30;
            isFirstTab = false;
        }
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        Minecraft client = Minecraft.getInstance();
        ScreenTabContext ctx = resolveContext(screen);
        if (ctx == null) return;
        if (expectingTabChange) {
            expectingTabChange = false;
            GLFW.glfwSetCursorPos(client.getWindow().getWindow(), savedMouseX, savedMouseY);
            MouseHandlerAccessor accessor = (MouseHandlerAccessor) client.mouseHandler;
            accessor.setXpos(savedMouseX);
            accessor.setYpos(savedMouseY);
        }
        int offset = getLegendaryTabsOffset(ctx.parentClass());
        boolean skipHomeTab = shouldSkipHomeTab(ctx.parentClass());
        int xPos = ctx.guiLeft() + offset;
        int topPos = ctx.guiTop() - 26;
        boolean isFirstTab = offset == 0;
        for (TabDefinition tab : ctx.tabs()) {
            if (!tab.shouldShow(client)) continue;
            if (skipHomeTab && tab.targetScreen() == ctx.parentClass()) continue;
            boolean isSelected = tab.targetScreen().isAssignableFrom(screen.getClass());
            int tabY = isSelected ? topPos - 2 : topPos;
            event.addListener(new TabButtonWidget(xPos, tabY, isFirstTab, isSelected, tab,
                    () -> handleTabClick(tab, client)));
            xPos += 30;
            isFirstTab = false;
        }
    }

    @SubscribeEvent
    public static void onScreenKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        Screen screen = event.getScreen();
        Minecraft client = Minecraft.getInstance();
        ScreenTabContext ctx = resolveContext(screen);
        if (ctx == null) return;
        int keyCode = event.getKeyCode();
        int scanCode = event.getScanCode();

        TabDefinition currentTab = ctx.tabs().stream()
                .filter(tab -> tab.targetScreen().isAssignableFrom(screen.getClass()))
                .findFirst()
                .orElse(null);

        if (client.options.keyInventory.matches(keyCode, scanCode)) {
            boolean alreadyHome = currentTab != null && currentTab.targetScreen() == ctx.parentClass();
            if (alreadyHome) return;
            if (currentTab != null && currentTab.canSwitchByKey()) {
                TabDefinition homeTab = findHomeTab(ctx);
                if (homeTab != null) {
                    handleTabClick(homeTab, client);
                    event.setCanceled(true);
                    return;
                }
            }
            screen.onClose();
            event.setCanceled(true);
            return;
        }
        for (TabDefinition tab : ctx.tabs()) {
            if (tab.keyMapping() == null || !tab.keyMapping().matches(keyCode, scanCode)) continue;
            if (!tab.shouldShow(client)) continue;
            boolean isSelected = tab == currentTab;
            if (isSelected) {
                screen.onClose();
                event.setCanceled(true);
            } else if (tab.canSwitchByKey()) {
                handleTabClick(tab, client);
                event.setCanceled(true);
            } else {
                screen.onClose();
                event.setCanceled(true);
            }
            return;
        }
    }

    @Nullable
    private static TabDefinition findHomeTab(ScreenTabContext ctx) {
        return ctx.tabs().stream()
                .filter(tab -> tab.targetScreen() == ctx.parentClass())
                .findFirst()
                .orElse(null);
    }

    private static void handleTabClick(TabDefinition tab, Minecraft mc) {
        if (tab.needsMouseFix()) {
            savedMouseX = mc.mouseHandler.xpos();
            savedMouseY = mc.mouseHandler.ypos();
            expectingTabChange = true;
        }
        tab.onTabClick().run();
    }
}