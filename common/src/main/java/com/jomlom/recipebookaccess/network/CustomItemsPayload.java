package com.jomlom.recipebookaccess.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CustomItemsPayload {
    private final List<ItemStack> itemStacks;
    private final boolean active;

    public static final ResourceLocation ID = NetworkConstants.ITEMS_PACKET_ID;

    public CustomItemsPayload(List<ItemStack> itemStacks, boolean active) {
        this.itemStacks = itemStacks.stream()
                .filter(stack -> !stack.isEmpty())
                .collect(Collectors.toList());
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public List<ItemStack> getItemStacks() {
        return itemStacks;
    }

    public static void encode(CustomItemsPayload payload, FriendlyByteBuf buf) {
        buf.writeInt(payload.itemStacks.size());
        for (ItemStack stack : payload.itemStacks) {
            buf.writeItem(stack);
        }
        buf.writeBoolean(payload.active);
    }

    public static CustomItemsPayload decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<ItemStack> stacks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            stacks.add(buf.readItem());
        }
        return new CustomItemsPayload(stacks, buf.readBoolean());
    }
}
