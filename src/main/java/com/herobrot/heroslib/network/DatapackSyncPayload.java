package com.herobrot.heroslib.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public record DatapackSyncPayload(String modId, String packetIdentifier, String jsonPayload) implements CustomPacketPayload {

    public static final Type<DatapackSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("heroslib", "datapack_sync"));

    // Aplicado el cambio crítico: Límites personalizados para evitar la desconexión del cliente por JSONs masivos
    public static final StreamCodec<FriendlyByteBuf, DatapackSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(256), DatapackSyncPayload::modId,
            ByteBufCodecs.stringUtf8(256), DatapackSyncPayload::packetIdentifier,
            ByteBufCodecs.stringUtf8(Integer.MAX_VALUE), DatapackSyncPayload::jsonPayload, // Límite abierto para Datapacks
            DatapackSyncPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}