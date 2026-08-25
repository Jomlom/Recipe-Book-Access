package com.jomlom.recipebookaccess.fabric;

import com.jomlom.recipebookaccess.RecipeBookAccessCommon;
import com.jomlom.recipebookaccess.network.CustomItemsPayload;
import com.jomlom.recipebookaccess.network.RequestItemsPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class RecipeBookAccessFabric implements ModInitializer {

	@Override
	public void onInitialize() {

		PayloadTypeRegistry.playS2C().register(CustomItemsPayload.ID, CustomItemsPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(RequestItemsPayload.ID, RequestItemsPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(RequestItemsPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				List<ItemStack> items = RecipeBookAccessCommon.collectAutofillItems(context.player());
				ServerPlayNetworking.send(context.player(), new CustomItemsPayload(items));
			});
		});
	}

}
