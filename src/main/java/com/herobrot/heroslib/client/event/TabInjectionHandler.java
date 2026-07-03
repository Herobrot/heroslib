package com.herobrot.heroslib.client.event;

import com.herobrot.heroslib.client.tab.TabDefinition;
import com.herobrot.heroslib.client.tab.TabRegistry;
import com.herobrot.heroslib.client.widget.TabButtonWidget;
import com.herobrot.heroslib.mixin.MouseHandlerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
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

        if (screen instanceof AbstractContainerScreen<?> containerScreen) {

            // 1. Integración del Mixin: Restaurar posición del cursor
            if (expectingTabChange) {
                expectingTabChange = false;
                GLFW.glfwSetCursorPos(client.getWindow().getWindow(), savedMouseX, savedMouseY);

                // Conexión del Accessor que estaba en desuso
                MouseHandlerAccessor accessor = (MouseHandlerAccessor) client.mouseHandler;
                accessor.setXpos(savedMouseX);
                accessor.setYpos(savedMouseY);
            }

            // 2. Determinar qué pestañas renderizar (Lógica LibZ)
            List<TabDefinition> activeTabs;
            if (screen instanceof ITabbedScreen tabbedScreen && tabbedScreen.getParentScreenClass() != null) {
                activeTabs = TabRegistry.getTabsFor(tabbedScreen.getParentScreenClass());
            } else {
                activeTabs = TabRegistry.getInventoryTabs();
            }

            if (activeTabs.isEmpty()) return;

            // 3. Coordenadas y Renderizado
            int xPos = containerScreen.getGuiLeft();
            int topPos = containerScreen.getGuiTop();
            boolean isFirstTab = true;

            for (TabDefinition tab : activeTabs) {
                if (tab.shouldShow(client)) { // Uso de la clase TabDefinition
                    boolean isSelected = tab.targetScreen().isAssignableFrom(screen.getClass());

                    // Si está seleccionada, la pestaña sube un poco más para resaltar
                    int tabY = topPos - (isSelected ? 28 : 25);

                    // Pasamos toda la instancia de 'tab' al widget para simplificar
                    event.addListener(new TabButtonWidget(xPos, tabY, isFirstTab, isSelected, tab, () -> handleTabClick(tab, client)));

                    xPos += 29; // Avanza en X para la siguiente pestaña
                    isFirstTab = false;
                }
            }
        }
    }

    private static void handleTabClick(TabDefinition tab, Minecraft mc) {
        savedMouseX = mc.mouseHandler.xpos();
        savedMouseY = mc.mouseHandler.ypos();
        expectingTabChange = true;

        if (tab.id().getPath().equals("inventory")) {
            assert mc.player != null;
            mc.player.closeContainer();
        } else if (tab.screenSupplier() != null) {
            mc.setScreen(tab.screenSupplier().get());
        }
    }
}