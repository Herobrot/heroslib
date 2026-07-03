package com.herobrot.heroslib.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = "heroslib")
public class PayloadRegistryManager {

    private static final List<PayloadData<?>> PAYLOAD_QUEUE = new ArrayList<>();

    // Se usa ? super RegistryFriendlyByteBuf para que acepte tanto FriendlyByteBuf normal como RegistryFriendlyByteBuf
    private record PayloadData<T extends CustomPacketPayload>(
            String modId,
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            IPayloadHandler<T> clientHandler,
            IPayloadHandler<T> serverHandler
    ) {}

    public static <T extends CustomPacketPayload> void registerBidirectional(
            String modId,
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            IPayloadHandler<T> clientHandler,
            IPayloadHandler<T> serverHandler
    ) {
        PAYLOAD_QUEUE.add(new PayloadData<>(modId, type, codec, clientHandler, serverHandler));
    }

    @SubscribeEvent
    public static void registerHandlers(final RegisterPayloadHandlersEvent event) {
        for (PayloadData<?> data : PAYLOAD_QUEUE) {
            registerSafely(event, data);
        }
    }

    private static <T extends CustomPacketPayload> void registerSafely(RegisterPayloadHandlersEvent event, PayloadData<T> data) {
        PayloadRegistrar registrar = event.registrar(data.modId()).versioned("1.0");
        registrar.playBidirectional(
                data.type(),
                data.codec(),
                new DirectionalPayloadHandler<>(data.clientHandler(), data.serverHandler())
        );
    }
}