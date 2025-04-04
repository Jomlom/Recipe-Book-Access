package com.jomlom.recipebookaccess.network;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CustomItemsPayload {
    private final List<ItemStack> itemStacks;

    public static final Identifier ID = NetworkConstants.ITEMS_PACKET_ID;

    public CustomItemsPayload(List<ItemStack> itemStacks) {
        this.itemStacks = itemStacks.stream()
                .filter(stack -> !stack.isEmpty())
                .collect(Collectors.toList());
    }

    public List<ItemStack> getItemStacks() {
        return itemStacks;
    }

    public static void encode(CustomItemsPayload payload, PacketByteBuf buf) {
        buf.writeInt(payload.itemStacks.size());
        for (ItemStack stack : payload.itemStacks) {
            buf.writeItemStack(stack);
        }
    }

    public static CustomItemsPayload decode(PacketByteBuf buf) {
        int size = buf.readInt();
        List<ItemStack> stacks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            stacks.add(buf.readItemStack());
        }
        return new CustomItemsPayload(stacks);
    }
}
