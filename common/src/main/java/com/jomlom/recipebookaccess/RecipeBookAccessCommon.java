package com.jomlom.recipebookaccess;

import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RecipeBookAccessCommon {

    public static final String MOD_ID = "recipebookaccess";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static List<ItemStack> collectAutofillItems(ServerPlayer player) {
        AbstractContainerMenu handler = player.containerMenu;
        List<ItemStack> items = new ArrayList<>();
        if (handler instanceof RecipeBookInventoryProvider customPop) {
            for (Container inventory : customPop.getInventoriesForAutofill()) {
                for (int i = 0; i < inventory.getContainerSize(); i++) {
                    ItemStack stack = inventory.getItem(i);
                    if (!stack.isEmpty()) {
                        items.add(stack);
                    }
                }
            }
        }
        return items;
    }

    public static void handleTransferRecipe(ServerPlayer player, int containerId, String recipeId, boolean useMaxItems) {
        AbstractContainerMenu handler = player.containerMenu;
        if (handler.containerId != containerId || !(handler instanceof AbstractCraftingMenu craftingMenu)
                || !(handler instanceof RecipeBookInventoryProvider)) {
            return;
        }

        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, Identifier.parse(recipeId));
        player.level().recipeAccess().byKey(key).ifPresent(recipe ->
                craftingMenu.handlePlacement(useMaxItems, player.isCreative(), recipe, player.level(), player.getInventory()));
    }
}
