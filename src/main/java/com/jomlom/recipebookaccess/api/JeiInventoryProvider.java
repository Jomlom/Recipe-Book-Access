package com.jomlom.recipebookaccess.api;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import java.util.Map;

public interface JeiInventoryProvider {

    Map<Slot, ItemStack> getJeiInventoryMap();

}