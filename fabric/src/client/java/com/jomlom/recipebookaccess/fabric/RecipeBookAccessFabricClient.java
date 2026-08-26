package com.jomlom.recipebookaccess.fabric;

import com.jomlom.recipebookaccess.network.ClientItemsReciever;
import com.jomlom.recipebookaccess.network.CustomItemsPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class RecipeBookAccessFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {

		ClientPlayNetworking.registerGlobalReceiver(CustomItemsPayload.ID, (payload, context) -> {
			context.client().execute(() -> {
				ClientItemsReciever.recieveItems(context.client(), payload.itemStacks());
			});
		});

	}
}
