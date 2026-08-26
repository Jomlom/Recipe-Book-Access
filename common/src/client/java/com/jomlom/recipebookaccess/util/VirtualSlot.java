package com.jomlom.recipebookaccess.util;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class VirtualSlot extends Slot {
    public VirtualSlot(Container inventory, int index) {
        super(inventory, index, -1, -1);
        this.index = -1;
    }

    public boolean isVirtual() {
        return true;
    }
}
