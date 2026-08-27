package com.herobrot.heroslib.client.event;

import net.minecraft.client.gui.screens.Screen;

public interface ITabbedScreen {
    /**
     * Devuelve la clase de la pantalla padre para agrupar las pestañas.
     * Ejemplo: return InventoryScreen.class;
     */
    default Class<? extends Screen> heroslib$getParentScreenClass() { return null; }

    int heroslib$getGuiLeft();
    int heroslib$getGuiTop();
}