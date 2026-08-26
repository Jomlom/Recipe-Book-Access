package com.jomlom.recipebookaccess.neoforge;

import com.jomlom.recipebookaccess.network.RequestItemsPayload;
import com.jomlom.recipebookaccess.platform.ClientNetworkHelper;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class NeoForgeClientNetworkHelper implements ClientNetworkHelper {
    @Override
    public void requestItems() {
        ClientPacketDistributor.sendToServer(new RequestItemsPayload(1));
    }
}
