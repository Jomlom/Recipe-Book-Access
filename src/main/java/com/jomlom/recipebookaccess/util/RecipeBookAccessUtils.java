package com.jomlom.recipebookaccess.util;

import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.screen.slot.Slot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeBookAccessUtils {

    private static final Map<Slot, Inventory> originMap = new HashMap<>();

    public static void populateCustomRecipeFinder(RecipeMatcher recipeFinder, RecipeBookInventoryProvider customPopulator) {
        for (Inventory inventory : customPopulator.getInventoriesForAutofill()) {
            for (int i = 0; i < inventory.size(); i++) {
                recipeFinder.addInput(inventory.getStack(i));
            }
        }
    }

    public static void populateCustomRecipeFinder(RecipeMatcher recipeFinder, List<ItemStack> items) {
        for (ItemStack itemStack : items) {
            recipeFinder.addInput(itemStack);
        }
    }

    public static int customFillInputSlot(Slot slot, ItemStack stack, int count, RecipeBookInventoryProvider customPop) {
        ItemStack slotStack = slot.getStack();

        for (Inventory inv : customPop.getInventoriesForAutofill()) {
            int matchingIndex = getMatchingSlotForInventory(inv, stack, slotStack);
            if (matchingIndex != -1) {
                originMap.put(slot, inv);

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

    private static int getMatchingSlotForInventory(Inventory inv, ItemStack item, ItemStack stack) {
        for (int i = 0; i < inv.size(); ++i) {
            ItemStack currentStack = inv.getStack(i);
            if (!currentStack.isEmpty()
                    && currentStack.itemMatches(item.getRegistryEntry())
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

    public static boolean tryReturnItemToOrigin(Slot slot, ItemStack stack) {
        Inventory originInventory = originMap.get(slot);
        if (originInventory != null) {
            boolean inserted = insertStackIntoInventory(originInventory, stack);
            originMap.remove(slot);
            return inserted;
        }
        return false;
    }

    private static boolean insertStackIntoInventory(Inventory inv, ItemStack stack) {
        for (int i = 0; i < inv.size(); i++) {
            ItemStack invStack = inv.getStack(i);
            if (!invStack.isEmpty() && ItemStack.areItemsAndComponentsEqual(invStack, stack)) {
                int maxStackSize = Math.min(invStack.getMaxCount(), stack.getMaxCount());
                int availableSpace = maxStackSize - invStack.getCount();
                if (availableSpace > 0) {
                    int toTransfer = Math.min(availableSpace, stack.getCount());
                    invStack.increment(toTransfer);
                    stack.decrement(toTransfer);
                    if (stack.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        for (int i = 0; i < inv.size(); i++) {
            ItemStack invStack = inv.getStack(i);
            if (invStack.isEmpty()) {
                inv.setStack(i, stack.copy());
                stack.setCount(0);
                return true;
            }
        }
        return stack.isEmpty();
    }
}