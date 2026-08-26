package com.jomlom.recipebookaccess.platform;

import com.jomlom.recipebookaccess.RecipeBookAccessCommon;

import java.util.ServiceLoader;

public class ClientServices {

    public static final ClientNetworkHelper NETWORK = load(ClientNetworkHelper.class);

    private static <T> T load(Class<T> clazz) {
        T loaded = ServiceLoader.load(clazz).findFirst()
                .orElseThrow(() -> new RuntimeException("Failed to load service for " + clazz.getName()));
        RecipeBookAccessCommon.LOGGER.info("Loaded {} for service {}", loaded, clazz);
        return loaded;
    }
}
