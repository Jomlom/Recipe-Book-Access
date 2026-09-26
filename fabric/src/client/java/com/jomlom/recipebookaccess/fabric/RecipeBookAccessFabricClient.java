package com.jomlom.recipebookaccess.fabric;

import com.jomlom.recipebookaccess.network.ClientItemsReciever;
import com.jomlom.recipebookaccess.network.CustomItemsPayload;
import com.jomlom.recipebookaccess.network.ClientAutoRefresh;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class RecipeBookAccessFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {

		ClientPlayNetworking.registerGlobalReceiver(CustomItemsPayload.ID, (client, handler, buf, responseSender) -> {
			CustomItemsPayload payload = CustomItemsPayload.decode(buf);
			client.execute(() -> ClientItemsReciever.recieveItems(client, payload.getItemStacks(), payload.isActive()));
		});

		ClientTickEvents.END_CLIENT_TICK.register(ClientAutoRefresh::tick);

	}
}
