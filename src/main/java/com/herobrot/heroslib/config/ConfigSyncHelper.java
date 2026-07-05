package com.herobrot.heroslib.config;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.herobrot.heroslib.HerosLib;
import com.herobrot.heroslib.api.ConfigSync;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@SuppressWarnings("unused")
public class ConfigSyncHelper {

    public static final Gson SYNC_GSON = new GsonBuilder()
            .setExclusionStrategies(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes f) {
                    return f.getAnnotation(ConfigSync.ClientOnly.class) != null;
                }

                @Override
                public boolean shouldSkipClass(Class<?> clazz) { return false; }
            })
            .create();

    /**
     * Mezcla automáticamente los valores del servidor en la configuración local.
     * Soporta herencia y omite de forma estricta campos @ClientOnly, estáticos, finales o transitorios.

     * Si el servidor desea vaciar un dato (ej. borrar restricciones),
     * debe enviar colecciones vacías ([] o {}). Los valores nulos son ignorados estrictamente
     * para prevenir NullPointerExceptions por campos ausentes o desactualizados.
     */
    public static <T> void mergeServerConfig(T localConfig, T serverConfig) {
        if (localConfig == null || serverConfig == null) return;

        Class<?> currentClass = localConfig.getClass();

        // Recorremos la clase actual y todas sus superclases (herencia) hasta llegar a Object
        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                int modifiers = field.getModifiers();

                // 1. Filtro de Seguridad Java: Ignorar campos estáticos, finales o transitorios
                if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers) || Modifier.isTransient(modifiers)) {
                    continue;
                }

                // 2. Filtro Lógico: Ignorar campos exclusivos del cliente (UI, visuales)
                if (field.isAnnotationPresent(ConfigSync.ClientOnly.class)) {
                    continue;
                }

                try {
                    field.setAccessible(true);
                    Object serverValue = field.get(serverConfig);

                    // Solo sobrescribimos si el servidor envió un valor real (previene nulos accidentales en Gson)
                    if (serverValue != null) {
                        field.set(localConfig, serverValue);
                    }
                } catch (Exception e) {
                    HerosLib.LOGGER.error("HerosLib: Falló la sincronización automática del campo '{}' en la clase '{}'.",
                            field.getName(), currentClass.getSimpleName(), e);
                }
            }
            // Subimos un nivel en la jerarquía
            currentClass = currentClass.getSuperclass();
        }
    }
}