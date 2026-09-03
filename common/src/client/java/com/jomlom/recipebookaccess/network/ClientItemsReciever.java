package com.jomlom.recipebookaccess.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class ClientItemsReciever {

    private static List<ItemStack> itemStacks = new ArrayList<>();
    private static Runnable onUpdate = null;
    private static boolean requestPending = false;

    public static boolean isRequestPending() {
        return requestPending;
    }

    public static void setOnUpdate(Runnable callback) {
        onUpdate = callback;
        requestPending = true;
    }

    public static void recieveItems(Minecraft client, List<ItemStack> items) {
        client.execute(() -> {
            itemStacks = new ArrayList<>(items);
            requestPending = false;
            if (onUpdate != null) {
                Runnable callback = onUpdate;
                onUpdate = null;
                callback.run();
            }
        });
    }

    public static List<ItemStack> getItemStacks(){
        return itemStacks != null ? itemStacks : List.of();
    }
}
