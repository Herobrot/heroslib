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
     *
     * @param json       El objeto JSON a escribir.
     * @param subFolder  La subcarpeta dentro de config/ (Ej: "heroslevels/generated").
     * @param fileName   El nombre del archivo (Ej: "mining_restrictions.json").
     */
    public static void writeConfigAsync(JsonObject json, String subFolder, String fileName) {
        CompletableFuture.runAsync(() -> {
            try {
                Path configDir = FMLPaths.CONFIGDIR.get().resolve(subFolder);

                if (!Files.exists(configDir)) {
                    Files.createDirectories(configDir);
                }

                File file = configDir.resolve(fileName).toFile();

                try (FileWriter writer = new FileWriter(file, false)) {
                    PRETTY_GSON.toJson(json, writer);
                    HerosLib.LOGGER.info("[HerosLib]: Archivo JSON generado exitosamente en {}", file.getAbsolutePath());
                }

            } catch (IOException e) {
                HerosLib.LOGGER.error("[HerosLib]: Fallo al escribir el archivo JSON {}. Detalle: {}", fileName, e.getMessage());
            }
        });
    }
}