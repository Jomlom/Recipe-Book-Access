package com.jomlom.recipebookaccess;

import com.jomlom.recipebookaccess.network.CustomItemsPayload;
import com.jomlom.recipebookaccess.network.RequestItemsPayload;
import com.jomlom.recipebookaccess.network.ServerRequestReciever;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecipeBookAccess implements ModInitializer {

	public static final String MOD_ID = "recipebookaccess";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		PayloadTypeRegistry.playS2C().register(CustomItemsPayload.ID, CustomItemsPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(RequestItemsPayload.ID, RequestItemsPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(RequestItemsPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
					ServerRequestReciever.handleRequest(context.player());
			});
		});
	}

}