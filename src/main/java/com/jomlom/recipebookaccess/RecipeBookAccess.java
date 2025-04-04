package com.jomlom.recipebookaccess;

import com.jomlom.recipebookaccess.network.RequestItemsPayload;
import com.jomlom.recipebookaccess.network.ServerRequestReciever;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecipeBookAccess implements ModInitializer {

	public static final String MOD_ID = "recipebookaccess";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		ServerPlayNetworking.registerGlobalReceiver(RequestItemsPayload.ID, (server, player, handler, buf, responseSender) -> {
			server.execute(() -> {
				ServerRequestReciever.handleRequest(player);
			});
		});

	}
}