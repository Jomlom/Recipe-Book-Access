package com.jomlom.recipebookaccess.platform;

public interface ClientNetworkHelper {
    void requestItems();
    void transferRecipe(int containerId, String recipeId, boolean useMaxItems);
}
