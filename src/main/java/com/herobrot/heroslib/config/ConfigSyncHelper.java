package com.herobrot.heroslib.config;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.herobrot.heroslib.api.ConfigSync;

@SuppressWarnings("unused")
public class ConfigSyncHelper {
    /**
     * Utiliza esta instancia de Gson para convertir tu configuración a String antes de enviarla
     * por el Payload. Automáticamente ignorará los campos marcados con @ConfigSync.ClientOnly.
     */
    public static final Gson SYNC_GSON = new GsonBuilder()
            .setExclusionStrategies(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes f) {
                    return f.getAnnotation(ConfigSync.ClientOnly.class) != null;
                }

                @Override
                public boolean shouldSkipClass(Class<?> clazz) {
                    return false;
                }
            })
            .create();
}