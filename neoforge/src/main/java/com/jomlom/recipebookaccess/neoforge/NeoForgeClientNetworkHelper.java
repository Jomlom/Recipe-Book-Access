package com.jomlom.recipebookaccess.neoforge;

import com.jomlom.recipebookaccess.RecipeBookAccessCommon;
import com.jomlom.recipebookaccess.network.RequestItemsPayload;
import com.jomlom.recipebookaccess.platform.ClientNetworkHelper;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.lang.reflect.Array;
import java.lang.reflect.Method;

public class NeoForgeClientNetworkHelper implements ClientNetworkHelper {

    private static final Method SEND_TO_SERVER = resolveSendToServer();

    private static Method resolveSendToServer() {
        Class<CustomPacketPayload[]> varargsType = arrayType();
        for (String className : new String[]{
                "net.neoforged.neoforge.client.network.ClientPacketDistributor",
                "net.neoforged.neoforge.network.PacketDistributor"
        }) {
            try {
                Class<?> clazz = Class.forName(className);
                return clazz.getMethod("sendToServer", CustomPacketPayload.class, varargsType);
            } catch (ReflectiveOperationException ignored) {
            }
        }
        throw new IllegalStateException("No known NeoForge sendToServer method found on this version");
    }

    @SuppressWarnings("unchecked")
    private static Class<CustomPacketPayload[]> arrayType() {
        return (Class<CustomPacketPayload[]>) Array.newInstance(CustomPacketPayload.class, 0).getClass();
    }

    @Override
    public void requestItems() {
        try {
            SEND_TO_SERVER.invoke(null, new RequestItemsPayload(1), (Object) new CustomPacketPayload[0]);
        } catch (ReflectiveOperationException e) {
            RecipeBookAccessCommon.LOGGER.error("Failed to send RequestItemsPayload to server", e);
        }
    }
}
