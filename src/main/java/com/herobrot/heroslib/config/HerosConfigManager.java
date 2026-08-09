package com.herobrot.heroslib.config;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class HerosConfigManager {

    private static final Map<String, Consumer<String>> CLIENT_HANDLERS = new HashMap<>();

    /**
     * Registra un manejador para el mod (Dist.CLIENT).
     */
    public static void registerClientSync(String modId, Consumer<String> jsonConsumer) {
        CLIENT_HANDLERS.put(modId, jsonConsumer);
    }

    /**
     * Se ejecuta de forma segura en el Hilo Principal gracias al enqueueWork de HerosLib.java.
     */
    public static void handleClientSync(String modId, String jsonPayload) {
        if (CLIENT_HANDLERS.containsKey(modId))
            CLIENT_HANDLERS.get(modId).accept(jsonPayload);
    }
}