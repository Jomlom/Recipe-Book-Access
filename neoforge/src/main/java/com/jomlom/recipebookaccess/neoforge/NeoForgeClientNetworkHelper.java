package com.jomlom.recipebookaccess.neoforge;

import com.jomlom.recipebookaccess.network.RequestItemsPayload;
import com.jomlom.recipebookaccess.network.TransferRecipePayload;
import com.jomlom.recipebookaccess.platform.ClientNetworkHelper;

public class NeoForgeClientNetworkHelper implements ClientNetworkHelper {
    @Override
    public void requestItems() {
        RecipeBookAccessNeoForge.CHANNEL.sendToServer(new RequestItemsPayload(1));
    }

    @Override
    public void transferRecipe(int containerId, String recipeId, boolean useMaxItems) {
        RecipeBookAccessNeoForge.CHANNEL.sendToServer(new TransferRecipePayload(containerId, recipeId, useMaxItems));
    }
}
