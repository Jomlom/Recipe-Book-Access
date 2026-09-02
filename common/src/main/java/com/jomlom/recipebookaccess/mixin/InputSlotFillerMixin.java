package com.jomlom.recipebookaccess.mixin;

import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import com.jomlom.recipebookaccess.util.RecipeBookAccessUtils;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ServerPlaceRecipe.class)
public abstract class InputSlotFillerMixin {

    @Shadow private RecipeBookMenu menu;
    @Shadow private Inventory inventory;

    @Unique
    private List<Container> cachedInventories;

    @Inject(
            method = "recipeClicked",
            at = @At("HEAD")
    )
    private void onRecipeClickedStart(ServerPlayer player, RecipeHolder recipe, boolean placeAll, CallbackInfo ci) {
        if (menu instanceof RecipeBookInventoryProvider customPop) {
            cachedInventories = customPop.getInventoriesForAutofill();
        }
    }

    @Inject(
            method = "recipeClicked",
            at = @At("TAIL")
    )
    private void onRecipeClickedEnd(ServerPlayer player, RecipeHolder recipe, boolean placeAll, CallbackInfo ci) {
        cachedInventories = null;
    }

    @Redirect(
            method = "recipeClicked",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;fillStackedContents(Lnet/minecraft/world/entity/player/StackedContents;)V"
            )
    )
    private void redirectInventoryPopulate(
            Inventory instance,
            StackedContents finder
    )    {
        if (menu instanceof RecipeBookInventoryProvider && cachedInventories != null) {
            RecipeBookAccessUtils.populateCustomRecipeFinderFromInventories(finder, cachedInventories);
        } else {
            instance.fillStackedContents(finder);
        }
    }

    @Inject(
            method = "moveItemToGrid",
            at = @At("HEAD"), cancellable = true
    )
    private void onFillInputSlot(
            Slot slot, ItemStack stack, int count, CallbackInfoReturnable<Integer> cir
    ) {
        if (menu instanceof RecipeBookInventoryProvider && cachedInventories != null) {
            int customResult = RecipeBookAccessUtils.customFillInputSlot(slot, stack, count, cachedInventories);
            slot.setChanged();
            cir.setReturnValue(customResult);
        }
    }

    @Inject(
            method = "clearGrid",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onReturnInputs(CallbackInfo ci) {
        if (menu instanceof RecipeBookInventoryProvider customPop) {
            for (int i = 0; i < menu.getSize(); i++) {
                if (menu.shouldMoveToInventory(i)) {
                    Slot slot = menu.getSlot(i);
                    ItemStack stack = slot.getItem().copy();
                    boolean returned = RecipeBookAccessUtils.tryReturnItemToOrigin(slot, stack);
                    if (!returned) {
                        inventory.placeItemBackInInventory(stack, false);
                    }
                    slot.set(stack);
                }
            }
            menu.clearCraftingContent();
            ci.cancel();
        }
    }
}
