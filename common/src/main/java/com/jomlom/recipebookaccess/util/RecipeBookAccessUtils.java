package com.jomlom.recipebookaccess.util;

import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeBookAccessUtils {

    private static final Map<Slot, List<SlotOriginPortion>> originMap = new HashMap<>();

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

    public static void populateCustomRecipeFinderFromInventories(StackedContents recipeFinder, List<Container> inventories) {
        for (Container inventory : inventories) {
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                recipeFinder.accountStack(inventory.getItem(i));
            }
        }
    }

    // appends each source's portion instead of overwriting so multi-source slots are remembered
    public static void customFillInputSlot(Slot slot, ItemStack stack, RecipeBookInventoryProvider customPop) {
        customFillInputSlot(slot, stack, customPop.getInventoriesForAutofill());
    }

    public static void customFillInputSlot(Slot slot, ItemStack stack, List<Container> inventories) {
        ItemStack slotStack = slot.getItem();

        for (Container inv : inventories) {
            int matchingIndex = getMatchingSlotForInventory(inv, stack, slotStack);
            if (matchingIndex != -1) {
                ItemStack invStack = inv.getItem(matchingIndex);
                ItemStack removedStack = invStack.getCount() > 1
                        ? inv.removeItem(matchingIndex, 1)
                        : inv.removeItemNoUpdate(matchingIndex);

                originMap.computeIfAbsent(slot, unused -> new ArrayList<>())
                        .add(new SlotOriginPortion(inv, matchingIndex, 1));

                if (slotStack.isEmpty()) {
                    slot.set(removedStack);
                } else {
                    slotStack.grow(1);
                }
                return;
            }
        }
    }

    private static int getMatchingSlotForInventory(Container inv, ItemStack item, ItemStack stack) {
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack currentStack = inv.getItem(i);
            if (!currentStack.isEmpty()
                    && currentStack.is(item.getItemHolder())
                    && usableWhenFillingSlot(stack)
                    && (stack.isEmpty() || ItemStack.isSameItemSameTags(stack, currentStack))) {
                return i;
            }
        }
        return -1;
    }

    private static boolean usableWhenFillingSlot(ItemStack stack) {
        return !stack.isDamaged() && !stack.isEnchanted() && !stack.hasCustomHoverName();
    }

    // returns each portion to its exact origin slot first then falls back to the origin container
    public static boolean tryReturnItemToOrigin(Slot slot, ItemStack stack) {
        List<SlotOriginPortion> portions = originMap.remove(slot);
        if (portions == null) {
            return false;
        }

        for (SlotOriginPortion portion : portions) {
            if (stack.isEmpty()) break;

            ItemStack piece = stack.split(Math.min(portion.count(), stack.getCount()));
            returnToExactSlot(portion, piece);
            if (!piece.isEmpty()) {
                insertStackIntoInventory(portion.container(), piece);
            }
            if (!piece.isEmpty()) {
                stack.grow(piece.getCount());
            }
        }

        return stack.isEmpty();
    }

    private static void returnToExactSlot(SlotOriginPortion origin, ItemStack stack) {
        ItemStack exact = origin.container().getItem(origin.slotIndex());
        if (!exact.isEmpty() && !ItemStack.isSameItemSameTags(exact, stack)) {
            return;
        }

        int maxStackSize = Math.min(stack.getMaxStackSize(), exact.isEmpty() ? stack.getMaxStackSize() : exact.getMaxStackSize());
        int availableSpace = maxStackSize - exact.getCount();
        if (availableSpace <= 0) return;

        int toTransfer = Math.min(availableSpace, stack.getCount());
        if (exact.isEmpty()) {
            origin.container().setItem(origin.slotIndex(), stack.copyWithCount(toTransfer));
        } else {
            exact.grow(toTransfer);
        }
        stack.shrink(toTransfer);
    }

    private record SlotOriginPortion(Container container, int slotIndex, int count) {}

    private static boolean insertStackIntoInventory(Container inv, ItemStack stack) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack invStack = inv.getItem(i);
            if (!invStack.isEmpty() && ItemStack.isSameItemSameTags(invStack, stack)) {
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
