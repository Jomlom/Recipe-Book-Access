package com.jomlom.recipebookaccess.network;

import com.jomlom.recipebookaccess.RecipeBookAccess;
import net.minecraft.util.Identifier;

public class NetworkConstants {
    public static final Identifier ITEMS_PACKET_ID = Identifier.of(RecipeBookAccess.MOD_ID, "items_packet");
    public static final Identifier REQUEST_ITEMS_PACKET_ID = Identifier.of(RecipeBookAccess.MOD_ID, "request_items_packet");
}
