package com.jomlom.recipebookaccess.neoforge;

import com.jomlom.recipebookaccess.network.ClientAutoRefresh;
import com.jomlom.recipebookaccess.network.ClientItemsReciever;
import com.jomlom.recipebookaccess.network.CustomItemsPayload;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

public class RecipeBookAccessNeoForgeClient {

    public static void registerTick() {
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) {
                ClientAutoRefresh.tick(Minecraft.getInstance());
            }
        });
    }

    public static void handleCustomItems(CustomItemsPayload payload) {
        ClientItemsReciever.recieveItems(Minecraft.getInstance(), payload.getItemStacks(), payload.isActive());
    }
}
