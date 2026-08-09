package com.herobrot.heroslib.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record GenericConfigSyncPayload(String modId, String jsonConfig) implements CustomPacketPayload {

    public static final Type<GenericConfigSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("heroslib", "generic_config_sync"));

    public static final StreamCodec<FriendlyByteBuf, GenericConfigSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, GenericConfigSyncPayload::modId,
            ByteBufCodecs.STRING_UTF8, GenericConfigSyncPayload::jsonConfig,
            GenericConfigSyncPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}