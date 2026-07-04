package com.herobrot.heroslib.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.herobrot.heroslib.HerosLib;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class JsonFileWriter {

    private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Escribe un JsonObject en la carpeta config del servidor/cliente de forma asíncrona.
     * NOTA DE USO: Esta es una herramienta de desarrollo para generar plantillas de configuración.
     * Los archivos generados aquí deben ser movidos manualmente a un Datapack activo (carpeta data/mod_id)
     * por el usuario para que el Mod los lea en el siguiente /reload.
     *
     * @param json       El objeto JSON a escribir.
     * @param subFolder  La subcarpeta dentro de config/ (Ej: "heroslevels/generated").
     * @param fileName   El nombre del archivo (Ej: "mining_restrictions.json").
     */
    public static void writeConfigAsync(JsonObject json, String subFolder, String fileName) {
        CompletableFuture.runAsync(() -> {
            try {
                // Obtener ruta base config/ del juego
                Path configDir = FMLPaths.CONFIGDIR.get().resolve(subFolder);

                // Crear directorios si no existen
                if (!Files.exists(configDir)) {
                    Files.createDirectories(configDir);
                }

                File file = configDir.resolve(fileName).toFile();

                // Bloque sincronizado para evitar corrupción si el comando se ejecuta múltiples veces en el mismo milisegundo
                synchronized (JsonFileWriter.class) {
                    try (FileWriter writer = new FileWriter(file, false)) { // false = sobrescribir
                        PRETTY_GSON.toJson(json, writer);
                        HerosLib.LOGGER.info("HerosLib: Archivo de plantilla JSON exportado exitosamente en {}", file.getAbsolutePath());
                    }
                }

            } catch (IOException e) {
                HerosLib.LOGGER.error("HerosLib: Fallo al escribir la plantilla JSON {}. Detalle: {}", fileName, e.getMessage());
            }
        });
    }
}