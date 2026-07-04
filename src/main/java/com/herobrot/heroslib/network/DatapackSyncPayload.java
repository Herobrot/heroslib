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

    public static final StreamCodec<FriendlyByteBuf, DatapackSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, DatapackSyncPayload::modId,
            ByteBufCodecs.STRING_UTF8, DatapackSyncPayload::packetIdentifier,
            ByteBufCodecs.STRING_UTF8, DatapackSyncPayload::jsonPayload, // El JSON gigante con todos los datos
            DatapackSyncPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}