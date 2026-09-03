package com.jomlom.recipebookaccess.neoforge;

import com.jomlom.recipebookaccess.network.ClientItemsReciever;
import com.jomlom.recipebookaccess.network.CustomItemsPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RecipeBookAccessNeoForgeClient {

    public static void registerClientPayloads(PayloadRegistrar registrar) {
        registrar.playToClient(CustomItemsPayload.ID, CustomItemsPayload.CODEC, (payload, context) -> {
            context.enqueueWork(() -> ClientItemsReciever.recieveItems(Minecraft.getInstance(), payload.itemStacks()));
        });
    }
}
