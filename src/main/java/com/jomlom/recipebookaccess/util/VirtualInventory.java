package com.jomlom.recipebookaccess.util;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;

public class VirtualInventory implements Inventory {
    private final DefaultedList<ItemStack> contents;

    public VirtualInventory(List<ItemStack> stacks) {
        contents = DefaultedList.ofSize(stacks.size(), ItemStack.EMPTY);
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            contents.set(i, stack.copy());
        }
    }

    @Override
    public int size() {
        return contents.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : contents) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return slot >= 0 && slot < contents.size() ? contents.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack current = getStack(slot);
        if (current.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (amount >= current.getCount()) {
            return removeStack(slot);
        }
        ItemStack split = current.split(amount);
        markDirty();
        return split;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack current = getStack(slot);
        if (current.isEmpty()) {
            return ItemStack.EMPTY;
        }
        contents.set(slot, ItemStack.EMPTY);
        markDirty();
        return current;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot >= 0 && slot < contents.size()) {
            contents.set(slot, stack);
            markDirty();
        }
    }

    @Override
    public void markDirty() {
        // no
    }

    @Override
    public boolean canPlayerUse(net.minecraft.entity.player.PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        for (int i = 0; i < contents.size(); i++) {
            contents.set(i, ItemStack.EMPTY);
        }
    }
}