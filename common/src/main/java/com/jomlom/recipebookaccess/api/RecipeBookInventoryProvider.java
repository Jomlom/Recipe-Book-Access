package com.jomlom.recipebookaccess.api;

import net.minecraft.world.Container;
import java.util.List;

public interface RecipeBookInventoryProvider {

    List<Container> getInventoriesForAutofill();

    default boolean persistentInventory() {
        return false;
    }

}
