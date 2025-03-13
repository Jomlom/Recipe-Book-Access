package com.jomlom.recipebookaccess.api;

import net.minecraft.inventory.Inventory;

import java.util.List;

public interface RecipeBookInventoryProvider {

    List<Inventory> getInventoriesForAutofill();

}