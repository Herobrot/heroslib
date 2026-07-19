package com.herobrot.heroslib.config;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.herobrot.heroslib.HerosLib;
import com.herobrot.heroslib.api.ConfigSync;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

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

    public static <T> void mergeServerConfig(T localConfig, T serverConfig) {
        if (localConfig == null || serverConfig == null) return;

        Class<?> currentClass = localConfig.getClass();

        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                int modifiers = field.getModifiers();

                if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers) || Modifier.isTransient(modifiers)) {
                    continue;
                }

                if (field.isAnnotationPresent(ConfigSync.ClientOnly.class)) {
                    continue;
                }

                try {
                    field.setAccessible(true);
                    Object serverValue = field.get(serverConfig);

                    if (serverValue != null) {
                        // Novedad: Validar límites matemáticos antes de asignar
                        serverValue = clampIfBounded(field, serverValue);
                        field.set(localConfig, serverValue);
                    }
                } catch (Exception e) {
                    HerosLib.LOGGER.error("[HerosLib]: Fallo la sincronizacion automatica del campo '{}'.", field.getName(), e);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
    }

    /**
     * Revisa si el campo tiene límites definidos por Cloth Config y fuerza el valor a mantenerse dentro del rango.
     */
    private static Object clampIfBounded(Field field, Object value) {
        ConfigEntry.BoundedDiscrete bounds = field.getAnnotation(ConfigEntry.BoundedDiscrete.class);
        if (bounds == null || !(value instanceof Number number)) {
            return value;
        }

        long clamped = Math.max(bounds.min(), Math.min(bounds.max(), number.longValue()));

        return switch (value) {
            case Integer i -> (int) clamped;
            case Long l -> clamped;
            case Short i -> (short) clamped;
            default -> value;
        };

    }
}