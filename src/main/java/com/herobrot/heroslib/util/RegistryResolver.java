package com.herobrot.heroslib.util;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class RegistryResolver {
    /**
     * Resuelve un String (Ej: "minecraft:iron_sword" o "#minecraft:swords") a una lista de objetos.
     *
     * @param identifier  El string extraído del JSON.
     * @param registry    El registro (Ej: BuiltInRegistries.ITEM).
     * @param registryKey La llave del registro (Ej: Registries.ITEM).
     * @return Lista de objetos resueltos. Vacía si no existe.
     */
    public static <T> List<T> resolve(String identifier, Registry<T> registry, ResourceKey<? extends Registry<T>> registryKey) {
        if (identifier == null || identifier.isBlank()) return Collections.emptyList();

        List<T> result = new ArrayList<>();

        if (identifier.startsWith("#")) {
            // Es un Tag
            ResourceLocation tagLocation = ResourceLocation.parse(identifier.substring(1));
            TagKey<T> tagKey = TagKey.create(registryKey, tagLocation);

            // Extraemos todos los elementos que componen el Tag
            registry.getTag(tagKey).ifPresent(namedTag -> {
                for (Holder<T> holder : namedTag) {
                    result.add(holder.value());
                }
            });
        } else {
            // Es un ID directo
            ResourceLocation idLocation = ResourceLocation.parse(identifier);
            if (registry.containsKey(idLocation)) {
                result.add(registry.get(idLocation));
            }
        }

        return result;
    }
}