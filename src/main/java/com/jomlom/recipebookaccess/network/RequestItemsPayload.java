package com.jomlom.recipebookaccess.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class RequestItemsPayload {
    private final int i;

    public static final Identifier ID = NetworkConstants.REQUEST_ITEMS_PACKET_ID;

    public RequestItemsPayload(int i) {
        this.i = i;
    }

    public int getI() {
        return i;
    }

    public static void encode(RequestItemsPayload payload, PacketByteBuf buf) {
        buf.writeInt(payload.i);
    }

    public static RequestItemsPayload decode(PacketByteBuf buf) {
        return new RequestItemsPayload(buf.readInt());
    }
}
