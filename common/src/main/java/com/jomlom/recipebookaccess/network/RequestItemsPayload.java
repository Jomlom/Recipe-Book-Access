package com.jomlom.recipebookaccess.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RequestItemsPayload(int i) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RequestItemsPayload> ID = new CustomPacketPayload.Type<>(NetworkConstants.REQUEST_ITEMS_PACKET_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestItemsPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, RequestItemsPayload::i,
                    RequestItemsPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
