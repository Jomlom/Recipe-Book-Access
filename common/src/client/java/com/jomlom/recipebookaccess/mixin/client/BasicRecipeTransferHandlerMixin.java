package com.jomlom.recipebookaccess.mixin.client;

import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import com.jomlom.recipebookaccess.network.ClientItemsReciever;
import com.jomlom.recipebookaccess.platform.ClientServices;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.library.transfer.BasicRecipeTransferHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = BasicRecipeTransferHandler.class, remap = false)
public abstract class BasicRecipeTransferHandlerMixin {

    @Final @Shadow private IRecipeTransferHandlerHelper handlerHelper;

    @Inject(
            method = "transferRecipe",
            at = @At("HEAD"),
            cancellable = true
    )
    private <C extends AbstractContainerMenu, R> void onTransferRecipe(
            C container,
            R recipe,
            IRecipeSlotsView recipeSlotsView,
            Player player,
            boolean maxTransfer,
            boolean doTransfer,
            CallbackInfoReturnable<IRecipeTransferError> cir
    ) {
        if (!(container instanceof RecipeBookInventoryProvider) || !(recipe instanceof RecipeHolder<?> holder)) {
            return;
        }

        ClientServices.NETWORK.requestItems();

        List<ItemStack> available = ClientItemsReciever.getItemStacks();
        if (!available.isEmpty()) {
            for (IRecipeSlotView slotView : recipeSlotsView.getSlotViews(RecipeIngredientRole.INPUT)) {
                if (slotView.isEmpty()) continue;
                if (slotView.getItemStacks().noneMatch(required -> hasMatch(required, available))) {
                    Component message = Component.translatable("jei.tooltip.error.recipe.transfer.missing");
                    cir.setReturnValue(handlerHelper.createUserErrorForMissingSlots(message, List.of(slotView)));
                    return;
                }
            }
        }

        if (doTransfer) {
            ClientServices.NETWORK.transferRecipe(container.containerId, holder.id().location().toString(), maxTransfer);
        }

        cir.setReturnValue(null);
    }

    private static boolean hasMatch(ItemStack required, List<ItemStack> available) {
        for (ItemStack stack : available) {
            if (ItemStack.isSameItem(stack, required)) {
                return true;
            }
        }
        return false;
    }
}
