package com.herobrot.heroslib.client.event;

import com.herobrot.heroslib.client.tab.TabDefinition;
import com.herobrot.heroslib.client.tab.TabRegistry;
import com.herobrot.heroslib.client.widget.TabButtonWidget;
import com.herobrot.heroslib.mixin.MouseHandlerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@EventBusSubscriber(modid = "heroslib", value = Dist.CLIENT)
public class TabInjectionHandler {

    private static boolean expectingTabChange = false;
    private static double savedMouseX = 0;
    private static double savedMouseY = 0;

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        Minecraft client = Minecraft.getInstance();

        if (screen instanceof CreativeModeInventoryScreen) {
            return;
        }

        int guiLeft = 0;
        int guiTop = 0;
        boolean isValidScreen = false;
        Class<? extends Screen> parentClass = null;

        // 1. Extraer coordenadas y la clase padre
        if (screen instanceof InventoryScreen containerScreen) {
            guiLeft = containerScreen.getGuiLeft();
            guiTop = containerScreen.getGuiTop();
            isValidScreen = true;
            parentClass = InventoryScreen.class;
        } else if (screen instanceof ITabbedScreen tabbedScreen) {
            guiLeft = tabbedScreen.getGuiLeft();
            guiTop = tabbedScreen.getGuiTop();
            isValidScreen = true;
            parentClass = tabbedScreen.getParentScreenClass();
        }

        if (!isValidScreen || parentClass == null) return;

        // 2. Obtener lista unificada
        List<TabDefinition> activeTabs = TabRegistry.getTabsFor(parentClass);

        if (!activeTabs.isEmpty()) {
            if (expectingTabChange) {
                expectingTabChange = false;
                GLFW.glfwSetCursorPos(client.getWindow().getWindow(), savedMouseX, savedMouseY);
                MouseHandlerAccessor accessor = (MouseHandlerAccessor) client.mouseHandler;
                accessor.setXpos(savedMouseX);
                accessor.setYpos(savedMouseY);
            }

            boolean isFirstTab = true;
            int xPos = guiLeft;
            int topPos = guiTop - 28; // Las pestañas descansan sobre el borde de la GUI

            for (TabDefinition tab : activeTabs) {
                if (tab.shouldShow(client)) {
                    boolean isSelected = tab.targetScreen().isAssignableFrom(screen.getClass());

                    // Si está seleccionada sube ligeramente; si no, queda al ras del borde
                    int tabY = isSelected ? topPos - 2 : topPos;

                    event.addListener(new TabButtonWidget(xPos, tabY, isFirstTab, isSelected, tab, () -> handleTabClick(tab, client)));

                    xPos += 29;
                    isFirstTab = false;
                }
            }
        }
    }

    private static void handleTabClick(TabDefinition tab, Minecraft mc) {
        if (tab.needsMouseFix()) {
            savedMouseX = mc.mouseHandler.xpos();
            savedMouseY = mc.mouseHandler.ypos();
            expectingTabChange = true;
        }

        if (tab.screenSupplier() != null) {
            mc.setScreen(tab.screenSupplier().get());
        } else {
            if (mc.player != null) {
                mc.setScreen(new InventoryScreen(mc.player));
            }
        }
    }
}