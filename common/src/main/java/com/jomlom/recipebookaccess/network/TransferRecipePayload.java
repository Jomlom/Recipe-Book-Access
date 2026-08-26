package com.jomlom.recipebookaccess.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record TransferRecipePayload(int containerId, String recipeId, boolean useMaxItems) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<TransferRecipePayload> ID = new CustomPacketPayload.Type<>(NetworkConstants.TRANSFER_RECIPE_PACKET_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, TransferRecipePayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, TransferRecipePayload::containerId,
                    ByteBufCodecs.STRING_UTF8, TransferRecipePayload::recipeId,
                    ByteBufCodecs.BOOL, TransferRecipePayload::useMaxItems,
                    TransferRecipePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
