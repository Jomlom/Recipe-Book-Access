package com.jomlom.recipebookaccess.util;

import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeBookAccessUtils {

    private static final Map<Slot, Container> originMap = new HashMap<>();

    public static void populateStackedContents(StackedItemContents recipeFinder, List<ItemStack> items) {
        for (ItemStack itemStack : items) {
            recipeFinder.accountStack(itemStack);
        }
    }

    public static SyntheticInventory buildSyntheticInventory(Player player, RecipeBookInventoryProvider customPop) {
        Inventory synthetic = new Inventory(player);
        List<StackOrigin> origins = new ArrayList<>();

        outer:
        for (Container inv : customPop.getInventoriesForAutofill()) {
            for (int slotIndex = 0; slotIndex < inv.getContainerSize(); slotIndex++) {
                ItemStack stack = inv.getItem(slotIndex);
                if (stack.isEmpty()) continue;
                if (origins.size() >= synthetic.getContainerSize()) break outer;
                synthetic.setItem(origins.size(), stack.copy());
                origins.add(new StackOrigin(inv, slotIndex, stack.getCount()));
            }
        }

        return new SyntheticInventory(synthetic, origins);
    }

    public static void reconcileSyntheticInventory(SyntheticInventory synthetic, RecipeBookInventoryProvider customPop) {
        Inventory inv = synthetic.inventory;
        List<StackOrigin> origins = synthetic.origins;

        for (int i = 0; i < origins.size(); i++) {
            StackOrigin origin = origins.get(i);
            ItemStack current = inv.getItem(i);

            if (current.isEmpty()) {
                origin.container.removeItem(origin.slotIndex, origin.originalCount);
                continue;
            }

            ItemStack originNow = origin.container.getItem(origin.slotIndex);
            if (!originNow.isEmpty() && !ItemStack.isSameItemSameComponents(current, originNow)) {
                continue;
            }

            int delta = current.getCount() - origin.originalCount;
            if (delta < 0) {
                origin.container.removeItem(origin.slotIndex, -delta);
            } else if (delta > 0) {
                ItemStack toAdd = current.copyWithCount(delta);
                if (!insertStackIntoInventory(origin.container, toAdd) && !toAdd.isEmpty()) {
                    depositLeftover(toAdd, customPop, inv.player);
                }
            }
        }

        for (int i = origins.size(); i < inv.getContainerSize(); i++) {
            ItemStack extra = inv.getItem(i);
            if (!extra.isEmpty()) {
                depositLeftover(extra, customPop, inv.player);
            }
        }
    }

    private static void depositLeftover(ItemStack stack, RecipeBookInventoryProvider customPop, Player fallbackPlayer) {
        for (Container inv : customPop.getInventoriesForAutofill()) {
            if (stack.isEmpty()) return;
            insertStackIntoInventory(inv, stack);
        }
        if (!stack.isEmpty()) {
            fallbackPlayer.getInventory().placeItemBackInInventory(stack, false);
        }
    }

    public static void trackGridSlotOrigins(List<Slot> gridSlots, SyntheticInventory synthetic) {
        List<StackOrigin> origins = synthetic.origins;

        for (Slot slot : gridSlots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;

            for (StackOrigin origin : origins) {
                if (ItemStack.isSameItem(stack, origin.container.getItem(origin.slotIndex))) {
                    originMap.put(slot, origin.container);
                    break;
                }
            }
        }
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

    private record StackOrigin(Container container, int slotIndex, int originalCount) {}

    public static final class SyntheticInventory {
        public final Inventory inventory;
        public final List<StackOrigin> origins;

        private SyntheticInventory(Inventory inventory, List<StackOrigin> origins) {
            this.inventory = inventory;
            this.origins = origins;
        }
    }
}
