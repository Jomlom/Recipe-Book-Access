package com.jomlom.recipebookaccess.neoforge;

import com.jomlom.recipebookaccess.RecipeBookAccessCommon;
import com.jomlom.recipebookaccess.network.CustomItemsPayload;
import com.jomlom.recipebookaccess.network.RequestItemsPayload;
import com.jomlom.recipebookaccess.network.TransferRecipePayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.List;
import java.util.function.Supplier;

@Mod(RecipeBookAccessCommon.MOD_ID)
public class RecipeBookAccessNeoForge {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RecipeBookAccessCommon.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public RecipeBookAccessNeoForge() {
        FMLJavaModLoadingContext.get().getModEventBus();

        int id = 0;
        CHANNEL.registerMessage(id++, RequestItemsPayload.class, RequestItemsPayload::encode, RequestItemsPayload::decode, this::handleRequestItems);
        CHANNEL.registerMessage(id++, CustomItemsPayload.class, CustomItemsPayload::encode, CustomItemsPayload::decode, this::handleCustomItems);
        CHANNEL.registerMessage(id++, TransferRecipePayload.class, TransferRecipePayload::encode, TransferRecipePayload::decode, this::handleTransferRecipe);
    }

    private void handleRequestItems(RequestItemsPayload payload, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                List<ItemStack> items = RecipeBookAccessCommon.collectAutofillItems(player);
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new CustomItemsPayload(items));
            }
        });
        ctx.setPacketHandled(true);
    }

    private void handleCustomItems(CustomItemsPayload payload, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> RecipeBookAccessNeoForgeClient.handleCustomItems(payload)));
        ctx.setPacketHandled(true);
    }

    private void handleTransferRecipe(TransferRecipePayload payload, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                RecipeBookAccessCommon.handleTransferRecipe(player, payload.containerId(), payload.recipeId(), payload.useMaxItems());
            }
        });
        ctx.setPacketHandled(true);
    }
}
