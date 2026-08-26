package com.jomlom.recipebookaccess.network;

import com.jomlom.recipebookaccess.RecipeBookAccessCommon;
import net.minecraft.resources.Identifier;

public class NetworkConstants {
    public static final Identifier ITEMS_PACKET_ID = Identifier.fromNamespaceAndPath(RecipeBookAccessCommon.MOD_ID, "items_packet");
    public static final Identifier REQUEST_ITEMS_PACKET_ID = Identifier.fromNamespaceAndPath(RecipeBookAccessCommon.MOD_ID, "request_items_packet");
}
