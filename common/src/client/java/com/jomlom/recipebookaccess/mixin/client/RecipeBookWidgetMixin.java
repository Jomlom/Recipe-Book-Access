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
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookWidgetMixin {

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

	@Unique
	private void redirect(Inventory inventory, StackedContents recipeFinder) {
		RecipeBookComponent widget = (RecipeBookComponent)(Object)this;

		RecipeBookMenu<?> handler =
				((RecipeBookWidgetAccessor)widget).getCraftingScreenHandler();

		if (handler instanceof RecipeBookInventoryProvider) {
			ClientServices.NETWORK.requestItems();
			ClientItemsReciever.setOnUpdate(() -> {
				List<ItemStack> updatedItems = ClientItemsReciever.getItemStacks();
				RecipeBookAccessUtils.populateCustomRecipeFinder(recipeFinder, updatedItems);
				widget.recipesUpdated();
			});
		} else {
			inventory.fillStackedContents(recipeFinder);
		}
	}


}
