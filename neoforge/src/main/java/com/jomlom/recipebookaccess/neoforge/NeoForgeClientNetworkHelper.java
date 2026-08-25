package com.jomlom.recipebookaccess.neoforge;

import com.jomlom.recipebookaccess.network.RequestItemsPayload;
import com.jomlom.recipebookaccess.platform.ClientNetworkHelper;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoForgeClientNetworkHelper implements ClientNetworkHelper {
    @Override
    public void requestItems() {
        PacketDistributor.sendToServer(new RequestItemsPayload(1));
    }
}
