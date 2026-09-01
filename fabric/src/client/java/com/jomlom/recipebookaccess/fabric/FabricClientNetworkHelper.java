package com.jomlom.recipebookaccess.fabric;

import com.jomlom.recipebookaccess.network.RequestItemsPayload;
import com.jomlom.recipebookaccess.network.TransferRecipePayload;
import com.jomlom.recipebookaccess.platform.ClientNetworkHelper;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;

public class FabricClientNetworkHelper implements ClientNetworkHelper {
    @Override
    public void requestItems() {
        FriendlyByteBuf buf = PacketByteBufs.create();
        RequestItemsPayload.encode(new RequestItemsPayload(1), buf);
        ClientPlayNetworking.send(RequestItemsPayload.ID, buf);
    }

    @Override
    public void transferRecipe(int containerId, String recipeId, boolean useMaxItems) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        TransferRecipePayload.encode(new TransferRecipePayload(containerId, recipeId, useMaxItems), buf);
        ClientPlayNetworking.send(TransferRecipePayload.ID, buf);
    }
}
