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
    private static boolean registrationClosed = false;

    private record PayloadData<T extends CustomPacketPayload>(
            String modId,
            String protocolVersion, // Versión dinámica por mod
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            @Nullable IPayloadHandler<T> clientHandler,
            @Nullable IPayloadHandler<T> serverHandler
    ) {}

    /**
     * Encola el paquete y válida que el evento de registro no haya pasado.
     */
    private static <T extends CustomPacketPayload> void enqueue(PayloadData<T> data) {
        if (registrationClosed) {
            throw new IllegalStateException(
                    "[HerosLib]: Intento de registrar el paquete '" + data.type().id()
                            + "' después de RegisterPayloadHandlersEvent. Registra todos los paquetes "
                            + "durante la construcción del mod (constructor)."
            );
        }
        PAYLOAD_QUEUE.add(data);
    }

    /**
     * Paquetes de Servidor -> Cliente
     */
    public static <T extends CustomPacketPayload> void registerClientbound(
            String modId, String protocolVersion, CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec, IPayloadHandler<T> clientHandler) {
        enqueue(new PayloadData<>(modId, protocolVersion, type, codec, clientHandler, null));
    }

    /**
     * Paquetes de Cliente -> Servidor
     */
    public static <T extends CustomPacketPayload> void registerServerbound(
            String modId, String protocolVersion, CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec, IPayloadHandler<T> serverHandler) {
        enqueue(new PayloadData<>(modId, protocolVersion, type, codec, null, serverHandler));
    }

    /**
     * Paquetes de doble vía (Bidireccional)
     */
    public static <T extends CustomPacketPayload> void registerBidirectional(
            String modId, String protocolVersion, CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            IPayloadHandler<T> clientHandler, IPayloadHandler<T> serverHandler) {
        enqueue(new PayloadData<>(modId, protocolVersion, type, codec, clientHandler, serverHandler));
    }

    @SubscribeEvent
    public static void registerHandlers(final RegisterPayloadHandlersEvent event) {
        registrationClosed = true;

        Map<String, List<PayloadData<?>>> groupedByMod = PAYLOAD_QUEUE.stream()
                .collect(Collectors.groupingBy(PayloadData::modId));

        groupedByMod.forEach((modId, dataList) -> {
            String version = dataList.getFirst().protocolVersion();
            PayloadRegistrar registrar = event.registrar(modId).versioned(version);
            for (PayloadData<?> data : dataList)
                registerSafely(registrar, data);
        });
        PAYLOAD_QUEUE.clear();
    }

    private static <T extends CustomPacketPayload> void registerSafely(PayloadRegistrar registrar, PayloadData<T> data) {
        if (data.clientHandler() != null && data.serverHandler() != null)
            registrar.playBidirectional(data.type(), data.codec(),
                    new DirectionalPayloadHandler<>(data.clientHandler(), data.serverHandler()));
        else if (data.clientHandler() != null)
            registrar.playToClient(data.type(), data.codec(), data.clientHandler());
        else if (data.serverHandler() != null)
            registrar.playToServer(data.type(), data.codec(), data.serverHandler());
    }
}