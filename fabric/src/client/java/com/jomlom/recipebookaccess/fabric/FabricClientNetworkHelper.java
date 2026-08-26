package com.jomlom.recipebookaccess.fabric;

import com.jomlom.recipebookaccess.network.RequestItemsPayload;
import com.jomlom.recipebookaccess.platform.ClientNetworkHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class FabricClientNetworkHelper implements ClientNetworkHelper {
    @Override
    public void requestItems() {
        ClientPlayNetworking.send(new RequestItemsPayload(1));
    }
}
