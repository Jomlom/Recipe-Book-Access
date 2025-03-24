package com.jomlom.recipebookaccess.mixin;

import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import com.jomlom.recipebookaccess.util.RecipeBookAccessUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.InputSlotFiller;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InputSlotFiller.class)
public abstract class InputSlotFillerMixin {

    @Shadow private AbstractRecipeScreenHandler handler;
    @Shadow private PlayerInventory inventory;

    @Redirect(
            method = "fillInputSlots(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/recipe/RecipeEntry;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerInventory;populateRecipeFinder(Lnet/minecraft/recipe/RecipeMatcher;)V"
            )
    )
    private void redirectInventoryPopulate(
            PlayerInventory instance,
            RecipeMatcher finder
    )    {
        if (handler instanceof RecipeBookInventoryProvider customPop) {
            RecipeBookAccessUtils.populateCustomRecipeFinder(finder, customPop);
        } else {
            instance.populateRecipeFinder(finder);
        }
    }

    @Inject(
            method = "fillInputSlot",
            at = @At("HEAD"), cancellable = true
    )
    private void onFillInputSlot(
            Slot slot, ItemStack stack, int count, CallbackInfoReturnable<Integer> cir
    ) {
        if (handler instanceof RecipeBookInventoryProvider customPop) {
            int customResult = RecipeBookAccessUtils.customFillInputSlot(slot, stack, count, customPop);
            slot.markDirty();
            cir.setReturnValue(customResult);
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
