package com.jomlom.recipebookaccess;

import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class RecipeBookAccessCommon {

    public static final String MOD_ID = "recipebookaccess";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final Method SERVER_LEVEL_METHOD = resolveServerLevelMethod();

    private static Method resolveServerLevelMethod() {
        for (Method method : ServerPlayer.class.getMethods()) {
            if (method.getParameterCount() == 0 && method.getReturnType() == ServerLevel.class) {
                return method;
            }
        }
        throw new IllegalStateException("No known ServerPlayer level accessor found on this version");
    }

    private static ServerLevel serverLevel(ServerPlayer player) {
        try {
            return (ServerLevel) SERVER_LEVEL_METHOD.invoke(player);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

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

        ServerLevel level = serverLevel(player);
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, ResourceLocation.parse(recipeId));
        level.recipeAccess().byKey(key).ifPresent(recipe ->
                craftingMenu.handlePlacement(useMaxItems, player.isCreative(), recipe, level, player.getInventory()));
    }
}
