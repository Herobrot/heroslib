package com.herobrot.heroslib;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = HerosLib.MODID)
public class HerosLibClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        HerosLib.LOGGER.info("[HerosLib] Client Setup: Cargando gestor de pestañas e inyectores UI.");
    }
}