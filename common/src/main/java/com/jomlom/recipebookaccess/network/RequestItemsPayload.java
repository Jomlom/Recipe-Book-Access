package com.jomlom.recipebookaccess.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class RequestItemsPayload {
    private final int i;

    public static final ResourceLocation ID = NetworkConstants.REQUEST_ITEMS_PACKET_ID;

    public RequestItemsPayload(int i) {
        this.i = i;
    }

    public int getI() {
        return i;
    }

    public static void encode(RequestItemsPayload payload, FriendlyByteBuf buf) {
        buf.writeInt(payload.i);
    }

    public static RequestItemsPayload decode(FriendlyByteBuf buf) {
        return new RequestItemsPayload(buf.readInt());
    }
}
