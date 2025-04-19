package com.jomlom.recipebookaccess.util;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.Slot;

public class VirtualSlot extends Slot {
    public VirtualSlot(Inventory inventory, int index) {
        super(inventory, index, -1, -1);
        this.id = -1;
    }

    public boolean isVirtual() {
        return true;
    }
}
