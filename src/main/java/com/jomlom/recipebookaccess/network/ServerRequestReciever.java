package com.jomlom.recipebookaccess.network;

import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.ArrayList;
import java.util.List;

public class ServerRequestReciever {

    public static void handleRequest(ServerPlayerEntity player) {
        ScreenHandler handler = player.currentScreenHandler;
        if (handler instanceof RecipeBookInventoryProvider customPop){
            List<Inventory> inventories = customPop.getInventoriesForAutofill();
            List<ItemStack> items = new ArrayList<>();
            for (Inventory inventory : inventories){
                for (int i = 0; i < inventory.size(); i++) {
                    if (inventory.getStack(i) != ItemStack.EMPTY){
                        items.add(inventory.getStack(i));
                    }
                }
            }
            items.removeIf(ItemStack::isEmpty);
            PacketByteBuf buf = PacketByteBufs.create();
            CustomItemsPayload.encode(new CustomItemsPayload(items), buf);
            ServerPlayNetworking.send(player, CustomItemsPayload.ID, buf);
        }
    }

}
