package com.jomlom.recipebookaccess.network;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import java.util.ArrayList;
import java.util.List;

public record  CustomItemsPayload(List<ItemStack> itemStacks) implements CustomPayload {

    public static final CustomPayload.Id<CustomItemsPayload> ID = new CustomPayload.Id<>(NetworkConstants.ITEMS_PACKET_ID);

    public static final PacketCodec<RegistryByteBuf, CustomItemsPayload> CODEC =
            PacketCodec.tuple(
                PacketCodecs.collection(ArrayList::new, ItemStack.PACKET_CODEC),
                CustomItemsPayload::itemStacks,
                CustomItemsPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
