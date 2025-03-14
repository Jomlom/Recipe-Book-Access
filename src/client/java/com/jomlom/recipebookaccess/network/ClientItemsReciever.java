package com.jomlom.recipebookaccess.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class ClientItemsReciever {

    private static List<ItemStack> itemStacks = new ArrayList<>();
    private static Runnable onUpdate = null;

    public static void setOnUpdate(Runnable callback) {
        onUpdate = callback;
    }

    public static void recieveItems(MinecraftClient client, List<ItemStack> items) {
        client.execute(() -> {
            itemStacks = new ArrayList<>(items);
            if (onUpdate != null) {
                onUpdate.run();
                onUpdate = null;
            }
        });
    }

    public static List<ItemStack> getItemStacks(){
        return itemStacks != null ? itemStacks : List.of();
    }
}
