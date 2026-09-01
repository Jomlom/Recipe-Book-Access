package com.jomlom.recipebookaccess.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class TransferRecipePayload {
    private final int containerId;
    private final String recipeId;
    private final boolean useMaxItems;

    public static final ResourceLocation ID = NetworkConstants.TRANSFER_RECIPE_PACKET_ID;

    public TransferRecipePayload(int containerId, String recipeId, boolean useMaxItems) {
        this.containerId = containerId;
        this.recipeId = recipeId;
        this.useMaxItems = useMaxItems;
    }

    public int containerId() {
        return containerId;
    }

    public String recipeId() {
        return recipeId;
    }

    public boolean useMaxItems() {
        return useMaxItems;
    }

    public static void encode(TransferRecipePayload payload, FriendlyByteBuf buf) {
        buf.writeVarInt(payload.containerId);
        buf.writeUtf(payload.recipeId);
        buf.writeBoolean(payload.useMaxItems);
    }

    public static TransferRecipePayload decode(FriendlyByteBuf buf) {
        return new TransferRecipePayload(buf.readVarInt(), buf.readUtf(), buf.readBoolean());
    }
}
