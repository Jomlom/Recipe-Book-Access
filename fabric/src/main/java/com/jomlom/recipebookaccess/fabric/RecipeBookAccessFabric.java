package com.jomlom.recipebookaccess.fabric;

import com.jomlom.recipebookaccess.RecipeBookAccessCommon;
import com.jomlom.recipebookaccess.network.CustomItemsPayload;
import com.jomlom.recipebookaccess.network.RequestItemsPayload;
import com.jomlom.recipebookaccess.network.TransferRecipePayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class RecipeBookAccessFabric implements ModInitializer {

	@Override
	public void onInitialize() {

		PayloadTypeRegistry.clientboundPlay().register(CustomItemsPayload.ID, CustomItemsPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(RequestItemsPayload.ID, RequestItemsPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(TransferRecipePayload.ID, TransferRecipePayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(RequestItemsPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				List<ItemStack> items = RecipeBookAccessCommon.collectAutofillItems(context.player());
				ServerPlayNetworking.send(context.player(), new CustomItemsPayload(items));
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(TransferRecipePayload.ID, (payload, context) -> {
			context.server().execute(() ->
					RecipeBookAccessCommon.handleTransferRecipe(context.player(), payload.containerId(), payload.recipeId(), payload.useMaxItems()));
		});
	}

}
