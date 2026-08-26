package com.jomlom.recipebookaccess.neoforge;

import com.jomlom.recipebookaccess.RecipeBookAccessCommon;
import com.jomlom.recipebookaccess.network.CustomItemsPayload;
import com.jomlom.recipebookaccess.network.RequestItemsPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.List;

@Mod(RecipeBookAccessCommon.MOD_ID)
public class RecipeBookAccessNeoForge {

    public RecipeBookAccessNeoForge(IEventBus modEventBus) {
        modEventBus.addListener(this::registerPayloads);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(RequestItemsPayload.ID, RequestItemsPayload.CODEC, (payload, context) -> {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer serverPlayer) {
                    List<ItemStack> items = RecipeBookAccessCommon.collectAutofillItems(serverPlayer);
                    context.reply(new CustomItemsPayload(items));
                }
            });
        });

        if (FMLEnvironment.dist == Dist.CLIENT) {
            RecipeBookAccessNeoForgeClient.registerClientPayloads(registrar);
        }
    }
}
