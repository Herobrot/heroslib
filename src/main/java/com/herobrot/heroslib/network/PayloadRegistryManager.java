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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = "heroslib")
public class PayloadRegistryManager {

    private static final List<PayloadData<?>> PAYLOAD_QUEUE = new ArrayList<>();

    // Hacemos los handlers @Nullable para identificar la dirección real del paquete
    private record PayloadData<T extends CustomPacketPayload>(
            String modId,
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            @Nullable IPayloadHandler<T> clientHandler,
            @Nullable IPayloadHandler<T> serverHandler
    ) {}

    /**
     * Paquetes de Servidor -> Cliente
     */
    public static <T extends CustomPacketPayload> void registerClientbound(
            String modId, CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec, IPayloadHandler<T> clientHandler) {
        PAYLOAD_QUEUE.add(new PayloadData<>(modId, type, codec, clientHandler, null));
    }

    /**
     * Paquetes de Cliente -> Servidor
     */
    public static <T extends CustomPacketPayload> void registerServerbound(
            String modId, CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec, IPayloadHandler<T> serverHandler) {
        PAYLOAD_QUEUE.add(new PayloadData<>(modId, type, codec, null, serverHandler));
    }

    /**
     * Paquetes de doble vía
     */
    public static <T extends CustomPacketPayload> void registerBidirectional(
            String modId, CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            IPayloadHandler<T> clientHandler, IPayloadHandler<T> serverHandler) {
        PAYLOAD_QUEUE.add(new PayloadData<>(modId, type, codec, clientHandler, serverHandler));
    }

    @SubscribeEvent
    public static void registerHandlers(final RegisterPayloadHandlersEvent event) {
        // Agrupar por modId para instanciar un solo PayloadRegistrar por mod (Optimización)
        Map<String, List<PayloadData<?>>> groupedByMod = PAYLOAD_QUEUE.stream()
                .collect(Collectors.groupingBy(PayloadData::modId));

        groupedByMod.forEach((modId, dataList) -> {
            PayloadRegistrar registrar = event.registrar(modId).versioned("1.0");

            for (PayloadData<?> data : dataList) {
                registerSafely(registrar, data);
            }
        });

        // Liberar memoria vaciando la cola tras el registro (Optimización)
        PAYLOAD_QUEUE.clear();
    }

    private static <T extends CustomPacketPayload> void registerSafely(PayloadRegistrar registrar, PayloadData<T> data) {
        // Uso estricto de la API nativa sin handlers fantasma
        if (data.clientHandler() != null && data.serverHandler() != null) {
            registrar.playBidirectional(data.type(), data.codec(),
                    new DirectionalPayloadHandler<>(data.clientHandler(), data.serverHandler()));
        } else if (data.clientHandler() != null) {
            registrar.playToClient(data.type(), data.codec(), data.clientHandler());
        } else if (data.serverHandler() != null) {
            registrar.playToServer(data.type(), data.codec(), data.serverHandler());
        }
    }
}