package com.jomlom.recipebookaccess.fabric;

import com.jomlom.recipebookaccess.RecipeBookAccessCommon;
import com.jomlom.recipebookaccess.network.CustomItemsPayload;
import com.jomlom.recipebookaccess.network.RequestItemsPayload;
import com.jomlom.recipebookaccess.network.TransferRecipePayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class RecipeBookAccessFabric implements ModInitializer {

	@Override
	public void onInitialize() {

		ServerPlayNetworking.registerGlobalReceiver(RequestItemsPayload.ID, (server, player, handler, buf, responseSender) -> {
			server.execute(() -> {
				List<ItemStack> items = RecipeBookAccessCommon.collectAutofillItems(player);
				FriendlyByteBuf response = PacketByteBufs.create();
				CustomItemsPayload.encode(new CustomItemsPayload(items), response);
				ServerPlayNetworking.send(player, CustomItemsPayload.ID, response);
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(TransferRecipePayload.ID, (server, player, handler, buf, responseSender) -> {
			TransferRecipePayload payload = TransferRecipePayload.decode(buf);
			server.execute(() ->
					RecipeBookAccessCommon.handleTransferRecipe(player, payload.containerId(), payload.recipeId(), payload.useMaxItems()));
		});
	}

}
