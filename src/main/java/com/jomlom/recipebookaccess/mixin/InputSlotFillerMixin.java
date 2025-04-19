package com.jomlom.recipebookaccess.mixin;

import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import com.jomlom.recipebookaccess.util.RecipeBookAccessUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.recipe.InputSlotFiller")
public abstract class InputSlotFillerMixin<C extends Inventory> {

    @Shadow protected AbstractRecipeScreenHandler<C> handler;
    @Shadow protected PlayerInventory inventory;

    @Redirect(
            method = "fillInputSlots(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/recipe/Recipe;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerInventory;populateRecipeFinder(Lnet/minecraft/recipe/RecipeMatcher;)V"
            )
    )
    private void redirectInventoryPopulate(PlayerInventory instance, RecipeMatcher matcher) {
        if (handler instanceof RecipeBookInventoryProvider customPop) {
            RecipeBookAccessUtils.populateCustomRecipeFinder(matcher, customPop);
        } else {
            instance.populateRecipeFinder(matcher);
        }
    }

    @Inject(
            method = "fillInputSlot",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onFillInputSlot(Slot slot, ItemStack stack, CallbackInfo ci) {
        if (handler instanceof RecipeBookInventoryProvider customPop) {
            RecipeBookAccessUtils.customFillInputSlot(slot, stack, 1, customPop);
            slot.markDirty();
            ci.cancel();
        }
    }

    @Inject(
            method = "returnInputs",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onReturnInputs(CallbackInfo ci) {
        if (handler instanceof RecipeBookInventoryProvider) {
            for (int i = 0; i < handler.getCraftingSlotCount(); i++) {
                if (handler.canInsertIntoSlot(i)) {
                    Slot slot = handler.getSlot(i);
                    ItemStack stack = slot.getStack().copy();
                    boolean returned = RecipeBookAccessUtils.tryReturnItemToOrigin(slot, stack);
                    if (!returned) {
                        inventory.offer(stack, false);
                    }
                    slot.setStackNoCallbacks(stack);
                }
            }
            handler.clearCraftingSlots();
            ci.cancel();
        }
    }
}
