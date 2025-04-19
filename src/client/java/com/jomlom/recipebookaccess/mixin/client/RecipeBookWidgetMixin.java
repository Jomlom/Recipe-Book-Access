package com.jomlom.recipebookaccess.mixin.client;

import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import com.jomlom.recipebookaccess.network.ClientItemsReciever;
import com.jomlom.recipebookaccess.network.RequestItemsPayload;
import com.jomlom.recipebookaccess.util.RecipeBookAccessUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(RecipeBookWidget.class)
public abstract class RecipeBookWidgetMixin {

	@Redirect(
			method = "refreshInputs",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/player/PlayerInventory;populateRecipeFinder(Lnet/minecraft/recipe/RecipeMatcher;)V"
			)
	)
	private void redirectPopulateRecipeFinderRefresh(PlayerInventory inventory, RecipeMatcher recipeFinder) {
		redirect(inventory, recipeFinder);
	}

	@Redirect(
			method = "reset",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/player/PlayerInventory;populateRecipeFinder(Lnet/minecraft/recipe/RecipeMatcher;)V"
			)
	)
	private void redirectPopulateRecipeFinderReset(PlayerInventory inventory, RecipeMatcher recipeFinder) {
		redirect(inventory, recipeFinder);
	}

	@Unique
	private void redirect(PlayerInventory inventory, RecipeMatcher recipeFinder) {
		RecipeBookWidget widget = (RecipeBookWidget)(Object)this;

		AbstractRecipeScreenHandler handler =
				((RecipeBookWidgetAccessor)widget).getCraftingScreenHandler();

		if (handler instanceof RecipeBookInventoryProvider) {
			PacketByteBuf buf = PacketByteBufs.create();
			RequestItemsPayload payload = new RequestItemsPayload(1);
			RequestItemsPayload.encode(payload, buf);
			ClientPlayNetworking.send(RequestItemsPayload.ID, buf);
			ClientItemsReciever.setOnUpdate(() -> {
				List<ItemStack> updatedItems = ClientItemsReciever.getItemStacks();
				RecipeBookAccessUtils.populateCustomRecipeFinder(recipeFinder, updatedItems);
				widget.refresh();
			});
		} else {
			inventory.populateRecipeFinder(recipeFinder);
		}
	}
}