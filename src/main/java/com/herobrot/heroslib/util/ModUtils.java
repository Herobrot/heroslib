package com.herobrot.heroslib.util;

import com.herobrot.heroslib.HerosLib;
import net.neoforged.fml.ModList;

public class ModUtils {

    /**
     * Obtiene la versión actual de un mod registrada en su archivo toml/gradle de forma segura.
     *
     * @param modId El ID del mod a consultar.
     * @return La versión del mod en formato String, o "1.0.0" como fallback generando una advertencia en el log.
     */
    public static String getModVersion(String modId) {
        return ModList.get().getModContainerById(modId)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElseGet(() -> {
                    HerosLib.LOGGER.warn("[HerosLib]: No se encontró el mod '{}' al resolver su version; usando fallback '1.0.0'.", modId);
                    return "1.0.0";
                });
    }
}