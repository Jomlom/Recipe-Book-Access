package com.jomlom.recipebookaccess;

import com.jomlom.recipebookaccess.network.ClientItemsReciever;
import com.jomlom.recipebookaccess.network.CustomItemsPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class RecipeBookAccessClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {

		ClientPlayNetworking.registerGlobalReceiver(CustomItemsPayload.ID, (client, handler, buf, responseSender) -> {
			CustomItemsPayload payload = CustomItemsPayload.decode(buf);
			client.execute(() -> {
				ClientItemsReciever.recieveItems(client, payload.getItemStacks());
			});
		});

	}
}