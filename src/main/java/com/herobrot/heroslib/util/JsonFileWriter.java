package com.herobrot.heroslib.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herobrot.heroslib.HerosLib;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

@SuppressWarnings("unused")
public class JsonFileWriter {

    private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();

    // Un lock independiente por cada archivo (subFolder + fileName), no uno global,
    // para no serializar escrituras de archivos distintos entre sí innecesariamente.
    private static final ConcurrentHashMap<String, Object> FILE_LOCKS = new ConcurrentHashMap<>();

    /**
     * Escribe un JsonObject completo de forma asíncrona, sobrescribiendo el archivo.
     * Útil cuando ya se tiene el contenido final calculado y no depende del estado previo del archivo.
     */
    public static CompletableFuture<Void> writeConfigAsync(JsonObject json, String subFolder, String fileName) {
        Object lock = FILE_LOCKS.computeIfAbsent(subFolder + "/" + fileName, k -> new Object());
        return CompletableFuture.runAsync(() -> {
            synchronized (lock) {
                writeInternal(json, subFolder, fileName);
            }
        });
    }

    /**
     * Lee, modifica y vuelve a escribir un archivo JSON de forma atómica y asíncrona.
     * Ideal para comandos o lógica que necesita agregar/actualizar entradas sin pisar
     * cambios concurrentes de otra ejecución sobre el mismo archivo.
     *
     * @param subFolder subcarpeta dentro de config/ (ej. "heroslevels/generated")
     * @param fileName  nombre del archivo (ej. "generated_restrictions.json")
     * @param updater   función que recibe el JSON actual (vacío si no existía o no se pudo leer)
     *                  y devuelve el JSON ya modificado a guardar
     */
    public static CompletableFuture<Void> updateJsonAsync(String subFolder, String fileName, UnaryOperator<JsonObject> updater) {
        Object lock = FILE_LOCKS.computeIfAbsent(subFolder + "/" + fileName, k -> new Object());
        return CompletableFuture.runAsync(() -> {
            synchronized (lock) {
                Path configDir = FMLPaths.CONFIGDIR.get().resolve(subFolder);
                File file = configDir.resolve(fileName).toFile();

                JsonObject current = new JsonObject();
                if (file.exists()) {
                    try (FileReader reader = new FileReader(file)) {
                        JsonElement parsed = JsonParser.parseReader(reader);
                        if (parsed.isJsonObject()) {
                            current = parsed.getAsJsonObject();
                        }
                    } catch (Exception e) {
                        // FIX: ya no se ignora en silencio; si el JSON está corrupto, se avisa
                        // antes de sobrescribirlo con un objeto vacío.
                        HerosLib.LOGGER.warn("HerosLib: No se pudo parsear {} existente, se reconstruirá. Detalle: {}", fileName, e.getMessage());
                    }
                }

                JsonObject updated = updater.apply(current);
                writeInternal(updated, subFolder, fileName);
            }
        });
    }

    private static void writeInternal(JsonObject json, String subFolder, String fileName) {
        try {
            Path configDir = FMLPaths.CONFIGDIR.get().resolve(subFolder);
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
            File file = configDir.resolve(fileName).toFile();

            try (FileWriter writer = new FileWriter(file, false)) {
                PRETTY_GSON.toJson(json, writer);
                HerosLib.LOGGER.info("HerosLib: Archivo JSON actualizado en {}", file.getAbsolutePath());
            }
        } catch (IOException e) {
            HerosLib.LOGGER.error("HerosLib: Fallo al escribir {}. Detalle: {}", fileName, e.getMessage());
        }
    }
}