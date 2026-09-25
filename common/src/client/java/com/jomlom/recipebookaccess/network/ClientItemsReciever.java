package com.jomlom.recipebookaccess.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class ClientItemsReciever {

    private static List<ItemStack> itemStacks = new ArrayList<>();
    private static boolean active = false;
    private static Runnable onUpdate = null;

    public static void setOnUpdate(Runnable callback) {
        onUpdate = callback;
    }

    public static void clearOnUpdate() {
        onUpdate = null;
    }

    public static void recieveItems(Minecraft client, List<ItemStack> items, boolean active) {
        client.execute(() -> {
            itemStacks = new ArrayList<>(items);
            ClientItemsReciever.active = active;
            if (onUpdate != null) {
                onUpdate.run();
            }
        });
    }

    public static boolean isActive() {
        return active;
    }

    public static void reset() {
        active = false;
    }

    public static List<ItemStack> getItemStacks(){
        return itemStacks != null ? itemStacks : List.of();
    }
}
