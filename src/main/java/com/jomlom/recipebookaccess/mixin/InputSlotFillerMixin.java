package com.jomlom.recipebookaccess.mixin;

import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import com.jomlom.recipebookaccess.util.RecipeBookAccessUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.recipe.InputSlotFiller;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(InputSlotFiller.class)
public abstract class InputSlotFillerMixin {

    @Final
    @Shadow
    private InputSlotFiller.Handler<?> handler;
    @Final
    @Shadow
    private PlayerInventory inventory;

    @Redirect(
            method = "fill(Lnet/minecraft/recipe/InputSlotFiller$Handler;IILjava/util/List;Ljava/util/List;Lnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/recipe/RecipeEntry;ZZ)Lnet/minecraft/screen/AbstractRecipeScreenHandler$PostFillAction;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerInventory;populateRecipeFinder(Lnet/minecraft/recipe/RecipeFinder;)V"
            )
    )
    private static void redirectInventoryPopulate(
            PlayerInventory inventory,
            RecipeFinder recipeFinder,
            InputSlotFiller.Handler<?> handler,
            int width, int height,
            List<Slot> inputSlots, List<Slot> slotsToReturn,
            PlayerInventory inv,
            RecipeEntry<? extends Recipe<? extends RecipeInput>> recipe,
            boolean craftAll, boolean creative
    )    {
        ScreenHandler screenHandler = RecipeBookAccessUtils.getOuterScreenHandler(handler);
        if (screenHandler instanceof RecipeBookInventoryProvider customPop) {
            RecipeBookAccessUtils.populateCustomRecipeFinder(recipeFinder, customPop);
        } else {
            inventory.populateRecipeFinder(recipeFinder);
        }
    }

    @Inject(
            method = "fillInputSlot",
            at = @At("HEAD"), cancellable = true
    )
    private void onFillInputSlot(Slot slot, RegistryEntry<Item> item, int count, CallbackInfoReturnable<Integer> cir) {
        ScreenHandler screenHandler = RecipeBookAccessUtils.getOuterScreenHandler(handler);
        if (screenHandler instanceof RecipeBookInventoryProvider customPop) {
            int customResult = RecipeBookAccessUtils.customFillInputSlot(slot, item, count, inventory, customPop);
            cir.setReturnValue(customResult);
        }
    }

}
