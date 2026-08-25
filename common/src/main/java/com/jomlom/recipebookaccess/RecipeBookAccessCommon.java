package com.jomlom.recipebookaccess;

import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
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
}
