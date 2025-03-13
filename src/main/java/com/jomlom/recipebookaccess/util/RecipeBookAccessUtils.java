package com.jomlom.recipebookaccess.util;

import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.InputSlotFiller;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.lang.reflect.Field;

public class RecipeBookAccessUtils {

    public static void populateCustomRecipeFinder(RecipeFinder recipeFinder, RecipeBookInventoryProvider customPopulator) {
        for (Inventory inventory : customPopulator.getInventoriesForAutofill()){
            for (int i = 0; i < inventory.size(); i++) {
                recipeFinder.addInput(inventory.getStack(i));
            }
        }
    }

    public static int customFillInputSlot(Slot slot, RegistryEntry<Item> item, int count, PlayerInventory playerInventory, RecipeBookInventoryProvider customPop) {
        ItemStack slotStack = slot.getStack();

        for (Inventory inv : customPop.getInventoriesForAutofill()) {
            int matchingIndex = getMatchingSlotForInventory(inv, item, slotStack);
            if (matchingIndex != -1) {
                ItemStack invStack = inv.getStack(matchingIndex);
                ItemStack removedStack;

                if (count < invStack.getCount()) {
                    removedStack = inv.removeStack(matchingIndex, count);
                } else {
                    removedStack = inv.removeStack(matchingIndex);
                }

                int removedCount = removedStack.getCount();
                if (slotStack.isEmpty()) {
                    slot.setStackNoCallbacks(removedStack);
                } else {
                    slotStack.increment(removedCount);
                }
                return count - removedCount;
            }
        }
        return -1;
    }

    private static int getMatchingSlotForInventory(Inventory inv, RegistryEntry<Item> item, ItemStack stack) {
        for (int i = 0; i < inv.size(); ++i) {
            ItemStack currentStack = inv.getStack(i);
            if (!currentStack.isEmpty()
                    && currentStack.itemMatches(item)
                    && usableWhenFillingSlot(stack)
                    && (stack.isEmpty() || ItemStack.areItemsAndComponentsEqual(stack, currentStack))) {
                return i;
            }
        }
        return -1;
    }

    private static boolean usableWhenFillingSlot(ItemStack stack) {
        return !stack.isDamaged() && !stack.hasEnchantments() && !stack.contains(DataComponentTypes.CUSTOM_NAME);
    }

    public static ScreenHandler getOuterScreenHandler(InputSlotFiller.Handler<?> handler) {
        Class<?> clazz = handler.getClass();
        for (Field f : clazz.getDeclaredFields()) {
            f.setAccessible(true);
            try {
                Object value = f.get(handler);
                if (value instanceof ScreenHandler screenHandler) {
                    return screenHandler;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
