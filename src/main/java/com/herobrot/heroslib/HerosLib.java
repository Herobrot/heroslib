package com.herobrot.heroslib;

import com.herobrot.heroslib.config.HerosConfigManager;
import com.herobrot.heroslib.network.GenericConfigSyncPayload;
import com.herobrot.heroslib.network.PayloadRegistryManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(HerosLib.MODID)
public class HerosLib {
    public static final String MODID = "heroslib";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @SuppressWarnings("unused")
    public HerosLib(IEventBus modEventBus, ModContainer modContainer) {
        PayloadRegistryManager.registerBidirectional(
                MODID,
                GenericConfigSyncPayload.TYPE,
                GenericConfigSyncPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        HerosConfigManager.handleClientSync(payload.modId(), payload.jsonConfig())),
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        player.sendSystemMessage(Component.literal("Config sync not supported from client to server."));
                    }
                })
        );
    }
}