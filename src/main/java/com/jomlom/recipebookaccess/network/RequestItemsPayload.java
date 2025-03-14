package com.jomlom.recipebookaccess.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record RequestItemsPayload(int i) implements CustomPayload {

    public static final CustomPayload.Id<RequestItemsPayload> ID = new CustomPayload.Id<>(NetworkConstants.REQUEST_ITEMS_PACKET_ID);

    public static final PacketCodec<RegistryByteBuf, RequestItemsPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.INTEGER, RequestItemsPayload::i,
                    RequestItemsPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
