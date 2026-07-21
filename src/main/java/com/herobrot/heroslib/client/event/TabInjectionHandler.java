package com.herobrot.heroslib.client.event;

import com.herobrot.heroslib.client.tab.TabDefinition;
import com.herobrot.heroslib.client.tab.TabRegistry;
import com.herobrot.heroslib.client.widget.TabButtonWidget;
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

@EventBusSubscriber(modid = "heroslib", value = Dist.CLIENT)
public class TabInjectionHandler {

    private static boolean expectingTabChange = false;
    private static double savedMouseX = 0;
    private static double savedMouseY = 0;

    // AÑADIDO: parentClass para saber cuál es la pestaña "Home" de este grupo
    private record ScreenTabContext(int guiLeft, int guiTop, Class<? extends Screen> parentClass, List<TabDefinition> tabs) {}

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
            guiLeft = tabbedScreen.getGuiLeft();
            guiTop = tabbedScreen.getGuiTop();
            parentClass = tabbedScreen.getParentScreenClass();
            if (parentClass == null) return null;
        } else {
            return null;
        }

        List<TabDefinition> tabs = TabRegistry.getTabsFor(parentClass);
        return tabs.isEmpty() ? null : new ScreenTabContext(guiLeft, guiTop, parentClass, tabs);
    }

    @SubscribeEvent
    public static void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        Screen screen = event.getScreen();
        ScreenTabContext ctx = resolveContext(screen);
        if (ctx == null) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int xPos = ctx.guiLeft();
        int topPos = ctx.guiTop() - 28;

        for (TabDefinition tab : ctx.tabs()) {
            if (!tab.shouldShow(Minecraft.getInstance())) continue;

            boolean isSelected = tab.targetScreen().isAssignableFrom(screen.getClass());
            if (!isSelected) {
                TabButtonWidget.drawBackgroundStatic(graphics, xPos, topPos, false);
            }
            xPos += 29;
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

        boolean isFirstTab = true;
        int xPos = ctx.guiLeft();
        int topPos = ctx.guiTop() - 28;

        for (TabDefinition tab : ctx.tabs()) {
            if (tab.shouldShow(client)) {
                boolean isSelected = tab.targetScreen().isAssignableFrom(screen.getClass());
                int tabY = isSelected ? topPos - 2 : topPos;

                event.addListener(new TabButtonWidget(xPos, tabY, isFirstTab, isSelected, tab, () -> handleTabClick(tab, client)));

                xPos += 29;
                isFirstTab = false;
            }
        }
    }

    // Manejador de teclado global para pestañas
    @SubscribeEvent
    public static void onScreenKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        Screen screen = event.getScreen();
        Minecraft client = Minecraft.getInstance();

        ScreenTabContext ctx = resolveContext(screen);
        if (ctx == null) return;

        int keyCode = event.getKeyCode();
        int scanCode = event.getScanCode();

        // 1. Tecla de inventario (E) -> volver a la home del grupo
        if (client.options.keyInventory.matches(keyCode, scanCode)) {
            TabDefinition homeTab = findHomeTab(ctx);
            if (homeTab != null && !homeTab.targetScreen().isAssignableFrom(screen.getClass())) {
                handleTabClick(homeTab, client);
                event.setCanceled(true); // Cancelamos para que el juego no cierre la pantalla
            }
            // Si ya estamos en la home, no cancelamos. Dejamos que Vanilla cierre el inventario.
            return;
        }

        // 2. Atajo dedicado de alguna pestaña del grupo (Ej. "K" para Skills)
        for (TabDefinition tab : ctx.tabs()) {
            if (tab.keyMapping() == null || !tab.keyMapping().matches(keyCode, scanCode)) continue;
            if (!tab.shouldShow(client)) continue;

            boolean isSelected = tab.targetScreen().isAssignableFrom(screen.getClass());

            if (isSelected) {
                // OPCIÓN 1 (Toggle-Close): Si ya estamos en esta pestaña, la tecla la cierra.
                screen.onClose();
            } else {
                // Si no, cambiamos a ella
                handleTabClick(tab, client);
            }

            event.setCanceled(true);
            return;
        }
    }

    @Nullable
    private static TabDefinition findHomeTab(ScreenTabContext ctx) {
        // Gracias a que ahora tenemos parentClass en el contexto, buscar la home es 100% preciso
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

        if (tab.screenSupplier() != null) {
            mc.setScreen(tab.screenSupplier().get());
        } else if (mc.player != null) {
            mc.setScreen(new InventoryScreen(mc.player));
        }
    }
}