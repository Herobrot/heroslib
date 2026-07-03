package com.herobrot.heroslib.client.screen;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

@SuppressWarnings("unused")
public abstract class AbstractTabbedScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

    public AbstractTabbedScreen(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    // HerosLib Injection Handler detectará que esta clase hereda de AbstractContainerScreen
    // y le inyectará automáticamente las pestañas en el evento Init.Post,
    // por lo que no necesitas dibujarlas manualmente aquí.
}