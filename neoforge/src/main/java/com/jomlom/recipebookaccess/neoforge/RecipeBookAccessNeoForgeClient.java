package com.jomlom.recipebookaccess.neoforge;

import com.jomlom.recipebookaccess.network.ClientItemsReciever;
import com.jomlom.recipebookaccess.network.CustomItemsPayload;
import net.minecraft.client.Minecraft;

public class RecipeBookAccessNeoForgeClient {

    public static void handleCustomItems(CustomItemsPayload payload) {
        ClientItemsReciever.recieveItems(Minecraft.getInstance(), payload.getItemStacks());
    }
}
