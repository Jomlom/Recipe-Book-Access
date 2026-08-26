package com.jomlom.recipebookaccess.network;

import com.jomlom.recipebookaccess.RecipeBookAccessCommon;
import net.minecraft.resources.ResourceLocation;

public class NetworkConstants {
    public static final ResourceLocation ITEMS_PACKET_ID = ResourceLocation.fromNamespaceAndPath(RecipeBookAccessCommon.MOD_ID, "items_packet");
    public static final ResourceLocation REQUEST_ITEMS_PACKET_ID = ResourceLocation.fromNamespaceAndPath(RecipeBookAccessCommon.MOD_ID, "request_items_packet");
}
