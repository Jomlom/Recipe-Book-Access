package com.jomlom.recipebookaccess.mixin;

import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import com.jomlom.recipebookaccess.util.RecipeBookAccessUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class ScreenHandlerMixin {

    @Inject(
            method = "removed(Lnet/minecraft/world/entity/player/Player;)V",
            at = @At("HEAD")
    )
    private void onClosedInject(Player player, CallbackInfo ci) {
        if (!(player instanceof ServerPlayer)) return;
        AbstractContainerMenu handler = (AbstractContainerMenu) (Object) this;
        if (handler instanceof AbstractCraftingMenu screenHandler && screenHandler instanceof RecipeBookInventoryProvider customPop) {
            if (!customPop.persistentInventory()) {
                for (Slot slot : screenHandler.getInputGridSlots()) {
                    ItemStack stack = slot.getItem().copy();
                    boolean returned = RecipeBookAccessUtils.tryReturnItemToOrigin(slot, stack);
                    if (!returned) {
                        player.getInventory().placeItemBackInInventory(stack, false);
                    }
                    slot.set(stack);
                }
            }
        }
    }
}
