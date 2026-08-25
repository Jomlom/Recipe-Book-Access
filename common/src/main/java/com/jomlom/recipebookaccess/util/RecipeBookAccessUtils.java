package com.jomlom.recipebookaccess.util;

import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeBookAccessUtils {

    private static final Map<Slot, Container> originMap = new HashMap<>();

    public static void populateCustomRecipeFinder(StackedContents recipeFinder, RecipeBookInventoryProvider customPopulator) {
        for (Container inventory : customPopulator.getInventoriesForAutofill()) {
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                recipeFinder.accountStack(inventory.getItem(i));
            }
        }
    }

    public static void populateCustomRecipeFinder(StackedContents recipeFinder, List<ItemStack> items) {
        for (ItemStack itemStack : items) {
            recipeFinder.accountStack(itemStack);
        }
    }

    public static int customFillInputSlot(Slot slot, ItemStack stack, int count, RecipeBookInventoryProvider customPop) {
        ItemStack slotStack = slot.getItem();

        for (Container inv : customPop.getInventoriesForAutofill()) {
            int matchingIndex = getMatchingSlotForInventory(inv, stack, slotStack);
            if (matchingIndex != -1) {
                originMap.put(slot, inv);

                ItemStack invStack = inv.getItem(matchingIndex);
                ItemStack removedStack;
                if (count < invStack.getCount()) {
                    removedStack = inv.removeItem(matchingIndex, count);
                } else {
                    removedStack = inv.removeItemNoUpdate(matchingIndex);
                }

                int removedCount = removedStack.getCount();
                if (slotStack.isEmpty()) {
                    slot.set(removedStack);
                } else {
                    slotStack.grow(removedCount);
                }
                return count - removedCount;
            }
        }
        return -1;
    }

    private static int getMatchingSlotForInventory(Container inv, ItemStack item, ItemStack stack) {
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack currentStack = inv.getItem(i);
            if (!currentStack.isEmpty()
                    && currentStack.is(item.getItemHolder())
                    && usableWhenFillingSlot(stack)
                    && (stack.isEmpty() || ItemStack.isSameItemSameComponents(stack, currentStack))) {
                return i;
            }
        }
        return -1;
    }

    private static boolean usableWhenFillingSlot(ItemStack stack) {
        return !stack.isDamaged() && !stack.isEnchanted() && !stack.has(DataComponents.CUSTOM_NAME);
    }

    public static boolean tryReturnItemToOrigin(Slot slot, ItemStack stack) {
        Container originInventory = originMap.get(slot);
        if (originInventory != null) {
            boolean inserted = insertStackIntoInventory(originInventory, stack);
            originMap.remove(slot);
            return inserted;
        }
        return false;
    }

    private static boolean insertStackIntoInventory(Container inv, ItemStack stack) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack invStack = inv.getItem(i);
            if (!invStack.isEmpty() && ItemStack.isSameItemSameComponents(invStack, stack)) {
                int maxStackSize = Math.min(invStack.getMaxStackSize(), stack.getMaxStackSize());
                int availableSpace = maxStackSize - invStack.getCount();
                if (availableSpace > 0) {
                    int toTransfer = Math.min(availableSpace, stack.getCount());
                    invStack.grow(toTransfer);
                    stack.shrink(toTransfer);
                    if (stack.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack invStack = inv.getItem(i);
            if (invStack.isEmpty()) {
                inv.setItem(i, stack.copy());
                stack.setCount(0);
                return true;
            }
        }
        return stack.isEmpty();
    }
}
