package com.jomlom.recipebookaccess.util;

import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RecipeBookAccessUtils {

    private static final Map<Slot, List<OriginPortion>> originMap = new HashMap<>();

    public static void reconcileGridForRecipe(List<Slot> gridSlots, RecipeHolder recipe, Player player) {
        List<Ingredient> ingredients = recipe.value().placementInfo().ingredients();

        for (Slot slot : gridSlots) {
            ItemStack current = slot.getItem();
            if (current.isEmpty()) continue;

            boolean stillNeeded = false;
            for (Ingredient ingredient : ingredients) {
                if (ingredient.test(current)) {
                    stillNeeded = true;
                    break;
                }
            }
            if (stillNeeded) continue;

            ItemStack stack = current.copy();
            boolean returned = tryReturnItemToOrigin(slot, stack);
            if (!returned) {
                player.getInventory().placeItemBackInInventory(stack, false);
            }
            slot.set(stack);
        }
    }

    public static void populateStackedContents(StackedItemContents recipeFinder, List<ItemStack> items) {
        for (ItemStack itemStack : items) {
            recipeFinder.accountStack(itemStack);
        }
    }

    public static SyntheticInventory buildSyntheticInventory(Player player, RecipeBookInventoryProvider customPop) {
        Inventory synthetic = new Inventory(player, new EntityEquipment());
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

    public static Map<Slot, Integer> snapshotGridCounts(List<Slot> gridSlots) {
        Map<Slot, Integer> counts = new HashMap<>();
        for (Slot slot : gridSlots) {
            counts.put(slot, slot.getItem().getCount());
        }
        return counts;
    }

    // attributes only newly added units per slot and appends them to any portions already recorded
    public static void trackGridSlotOrigins(List<Slot> gridSlots, SyntheticInventory synthetic, Map<Slot, Integer> beforeCounts) {
        List<StackOrigin> origins = synthetic.origins;
        Inventory inv = synthetic.inventory;

        Map<StackOrigin, Integer> remainingConsumed = new LinkedHashMap<>();
        for (int i = 0; i < origins.size(); i++) {
            StackOrigin origin = origins.get(i);
            int consumed = origin.originalCount - inv.getItem(i).getCount();
            if (consumed > 0) {
                remainingConsumed.put(origin, consumed);
            }
        }

        for (Slot slot : gridSlots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;

            int before = beforeCounts.getOrDefault(slot, 0);
            int newlyAdded = stack.getCount() - before;
            if (newlyAdded <= 0) continue;

            List<OriginPortion> portions = new ArrayList<>();
            int remaining = newlyAdded;

            for (Map.Entry<StackOrigin, Integer> entry : remainingConsumed.entrySet()) {
                if (remaining <= 0) break;
                if (entry.getValue() <= 0) continue;
                StackOrigin origin = entry.getKey();
                if (!ItemStack.isSameItem(stack, origin.container.getItem(origin.slotIndex))) continue;

                int take = Math.min(remaining, entry.getValue());
                portions.add(new OriginPortion(origin, take));
                entry.setValue(entry.getValue() - take);
                remaining -= take;
            }

            if (!portions.isEmpty()) {
                originMap.computeIfAbsent(slot, unused -> new ArrayList<>()).addAll(portions);
            }
        }
    }

    public static void returnGridSlotsToOrigins(List<Slot> gridSlots, Player player) {
        for (Slot slot : gridSlots) {
            ItemStack stack = slot.getItem().copy();
            if (stack.isEmpty()) continue;

            boolean returned = tryReturnItemToOrigin(slot, stack);
            if (!returned) {
                player.getInventory().placeItemBackInInventory(stack, false);
            }
            slot.set(stack);
        }
    }

    // returns each portion to its exact origin slot first then falls back to the origin container
    public static boolean tryReturnItemToOrigin(Slot slot, ItemStack stack) {
        List<OriginPortion> portions = originMap.remove(slot);
        if (portions == null) {
            return false;
        }

        for (OriginPortion portion : portions) {
            if (stack.isEmpty()) break;

            ItemStack piece = stack.split(Math.min(portion.count, stack.getCount()));
            returnToExactSlot(portion.origin, piece);
            if (!piece.isEmpty()) {
                insertStackIntoInventory(portion.origin.container, piece);
            }
            if (!piece.isEmpty()) {
                stack.grow(piece.getCount());
            }
        }

        return stack.isEmpty();
    }

    private static void returnToExactSlot(StackOrigin origin, ItemStack stack) {
        ItemStack exact = origin.container.getItem(origin.slotIndex);
        if (!exact.isEmpty() && !ItemStack.isSameItemSameComponents(exact, stack)) {
            return;
        }

        int maxStackSize = Math.min(stack.getMaxStackSize(), exact.isEmpty() ? stack.getMaxStackSize() : exact.getMaxStackSize());
        int availableSpace = maxStackSize - exact.getCount();
        if (availableSpace <= 0) return;

        int toTransfer = Math.min(availableSpace, stack.getCount());
        if (exact.isEmpty()) {
            origin.container.setItem(origin.slotIndex, stack.copyWithCount(toTransfer));
        } else {
            exact.grow(toTransfer);
        }
        stack.shrink(toTransfer);
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

    private record OriginPortion(StackOrigin origin, int count) {}

    public static final class SyntheticInventory {
        public final Inventory inventory;
        public final List<StackOrigin> origins;

        private SyntheticInventory(Inventory inventory, List<StackOrigin> origins) {
            this.inventory = inventory;
            this.origins = origins;
        }
    }
}
