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

        // FILTRO: Ignorar el Inventario Creativo para evitar superposiciones
        if (screen instanceof CreativeModeInventoryScreen) {
            return;
        }

        int guiLeft = 0;
        int guiTop = 0;
        boolean isValidScreen = false;
        List<TabDefinition> activeTabs = null;

        // Caso 1: Pantallas nativas de Minecraft (AbstractContainerScreen)
        if (screen instanceof InventoryScreen containerScreen) {
            guiLeft = containerScreen.getGuiLeft();
            guiTop = containerScreen.getGuiTop();
            isValidScreen = true;
            activeTabs = TabRegistry.getInventoryTabs();
        }
        // Caso 2: Pantallas de tus Mods que implementan ITabbedScreen
        else if (screen instanceof ITabbedScreen tabbedScreen) {
            guiLeft = tabbedScreen.getGuiLeft();
            guiTop = tabbedScreen.getGuiTop();
            isValidScreen = true;
            // Preservamos la lógica de agrupamiento (ej. si la pestaña padre es el Yunke, trae las de Yunque)
            activeTabs = tabbedScreen.getParentScreenClass() != null
                    ? TabRegistry.getTabsFor(tabbedScreen.getParentScreenClass())
                    : TabRegistry.getInventoryTabs();
        }

        // Renderizado
        if (isValidScreen && activeTabs != null && !activeTabs.isEmpty()) {

            // Fix del ratón de TieredNeo
            if (expectingTabChange) {
                expectingTabChange = false;
                GLFW.glfwSetCursorPos(client.getWindow().getWindow(), savedMouseX, savedMouseY);
                MouseHandlerAccessor accessor = (MouseHandlerAccessor) client.mouseHandler;
                accessor.setXpos(savedMouseX);
                accessor.setYpos(savedMouseY);
            }

            boolean isFirstTab = true;
            int xPos = guiLeft;
            int topPos = guiTop - 28; // Altura exacta para sentarse sobre el borde

            for (TabDefinition tab : activeTabs) {
                if (tab.shouldShow(client)) {
                    boolean isSelected = tab.targetScreen().isAssignableFrom(screen.getClass());

                    // La seleccionada sube ligeramente (2 píxeles) y su base no se oculta
                    int tabY = isSelected ? topPos - 2 : topPos;

                    event.addListener(new TabButtonWidget(xPos, tabY, isFirstTab, isSelected, tab, () -> handleTabClick(tab, client)));

                    xPos += 29;
                    isFirstTab = false;
                }
            }
        }
    }

    private static void handleTabClick(TabDefinition tab, Minecraft mc) {
        // Solo aplicar el "Hack de TieredNeo" si la pestaña hace llamadas al servidor
        if (tab.needsMouseFix()) {
            savedMouseX = mc.mouseHandler.xpos();
            savedMouseY = mc.mouseHandler.ypos();
            expectingTabChange = true;
        }

        // Transición de pantalla nativa
        if (tab.screenSupplier() != null) {
            mc.setScreen(tab.screenSupplier().get());
        } else {
            if (mc.player != null) {
                // Abre el inventario del cliente (Vanilla maneja el ratón perfectamente aquí)
                mc.setScreen(new InventoryScreen(mc.player));
            }
        }
    }
}