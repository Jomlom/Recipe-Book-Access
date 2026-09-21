package com.jomlom.recipebookaccess.network;

import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import com.jomlom.recipebookaccess.platform.ClientServices;
import net.minecraft.client.Minecraft;

public class ClientAutoRefresh {

    private static final int REFRESH_INTERVAL_TICKS = 10;
    private static int ticksUntilNextRequest = 0;

    public static void tick(Minecraft client) {
        if (client.player == null || !(client.player.containerMenu instanceof RecipeBookInventoryProvider)) {
            ticksUntilNextRequest = 0;
            ClientItemsReciever.clearOnUpdate();
            return;
        }

        if (ticksUntilNextRequest > 0) {
            ticksUntilNextRequest--;
            return;
        }

        ticksUntilNextRequest = REFRESH_INTERVAL_TICKS;
        ClientServices.NETWORK.requestItems();
    }
}
