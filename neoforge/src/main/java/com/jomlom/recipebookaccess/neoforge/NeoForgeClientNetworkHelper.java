package com.jomlom.recipebookaccess.neoforge;

import com.jomlom.recipebookaccess.network.RequestItemsPayload;
import com.jomlom.recipebookaccess.network.TransferRecipePayload;
import com.jomlom.recipebookaccess.platform.ClientNetworkHelper;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoForgeClientNetworkHelper implements ClientNetworkHelper {
    @Override
    public void requestItems() {
        PacketDistributor.sendToServer(new RequestItemsPayload(1));
    }

    @Override
    public void transferRecipe(int containerId, String recipeId, boolean useMaxItems) {
        PacketDistributor.sendToServer(new TransferRecipePayload(containerId, recipeId, useMaxItems));
    }
}
