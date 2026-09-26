package com.jomlom.recipebookaccess.mixin.client;

import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import com.jomlom.recipebookaccess.network.ClientItemsReciever;
import com.jomlom.recipebookaccess.platform.ClientServices;
import com.jomlom.recipebookaccess.util.RecipeBookAccessUtils;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookWidgetMixin {

	@Inject(method = "updateStackedContents", at = @At("HEAD"), cancellable = true)
	private void deferToServerSnapshot(CallbackInfo ci) {
		RecipeBookMenu handler = ((RecipeBookWidgetAccessor)(Object)this).getCraftingScreenHandler();
		if (handler instanceof RecipeBookInventoryProvider && ClientItemsReciever.hasOnUpdate()) {
			ClientServices.NETWORK.requestItems();
			ci.cancel();
		}
	}

	@Redirect(
			method = "updateStackedContents",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/player/Inventory;fillStackedContents(Lnet/minecraft/world/entity/player/StackedContents;)V"
			)
	)
	private void redirectPopulateRecipeFinderRefresh(Inventory inventory, StackedContents recipeFinder) {
		redirect(inventory, recipeFinder);
	}

	@Redirect(
			method = "initVisuals",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/player/Inventory;fillStackedContents(Lnet/minecraft/world/entity/player/StackedContents;)V"
			)
	)
	private void redirectPopulateRecipeFinderReset(Inventory inventory, StackedContents recipeFinder) {
		redirect(inventory, recipeFinder);
	}

	@Redirect(
			method = "updateStackedContents",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/inventory/RecipeBookMenu;fillCraftSlotsStackedContents(Lnet/minecraft/world/entity/player/StackedContents;)V"
			)
	)
	private void redirectFillCraftSlotsRefresh(RecipeBookMenu handler, StackedContents recipeFinder) {
		fillCraftSlots(handler, recipeFinder);
	}

	@Redirect(
			method = "initVisuals",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/inventory/RecipeBookMenu;fillCraftSlotsStackedContents(Lnet/minecraft/world/entity/player/StackedContents;)V"
			)
	)
	private void redirectFillCraftSlotsReset(RecipeBookMenu handler, StackedContents recipeFinder) {
		fillCraftSlots(handler, recipeFinder);
	}

	@Unique
	private void fillCraftSlots(RecipeBookMenu handler, StackedContents recipeFinder) {
		if (!(handler instanceof RecipeBookInventoryProvider) || RecipeBookAccessUtils.gridSlots(handler).isEmpty()) {
			handler.fillCraftSlotsStackedContents(recipeFinder);
		}
	}

	@Unique
	private void redirect(Inventory inventory, StackedContents recipeFinder) {
		RecipeBookComponent widget = (RecipeBookComponent)(Object)this;

		RecipeBookMenu<?, ?> handler =
				((RecipeBookWidgetAccessor)widget).getCraftingScreenHandler();

		if (handler instanceof RecipeBookInventoryProvider) {
			RecipeBookAccessUtils.populateCustomRecipeFinder(recipeFinder, ClientItemsReciever.getItemStacks());

			ClientItemsReciever.setOnUpdate(() -> {
				List<ItemStack> updatedItems = ClientItemsReciever.getItemStacks();
				RecipeBookAccessUtils.populateCustomRecipeFinder(recipeFinder, updatedItems);
				widget.recipesUpdated();
			});

			ClientServices.NETWORK.requestItems();
		} else {
			inventory.fillStackedContents(recipeFinder);
		}
	}


}
