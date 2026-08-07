package com.herobrot.heroslib.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.herobrot.heroslib.HerosLib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Clase base abstracta para cargar Datapacks.
 * Los mods dependientes deben heredar esta clase e implementarla en el evento AddReloadListenerEvent.
 */
@SuppressWarnings("unused")
public abstract class AbstractDataLoader extends SimpleJsonResourceReloadListener {

    private final String folderName;

    public AbstractDataLoader(Gson gson, String folderName) {
        super(gson, folderName);
        this.folderName = folderName;
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> objectMap, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        HerosLib.LOGGER.info("[HerosLib]: Cargando datos desde la carpeta de datapacks: {}", this.folderName);
        this.clearLocalData();
        objectMap.forEach((id, element) -> {
            try {
                if (element.isJsonObject()) {
                    JsonObject jsonObject = element.getAsJsonObject();
                    this.processJson(id, jsonObject);
                }
            } catch (Exception e) {
                HerosLib.LOGGER.error("[HerosLib]: Error cargando el recurso {} en la carpeta {}. Detalle: {}",
                        id, this.folderName, e.getMessage());
            }
        });
        this.onDataLoaded();
    }

    /**
     * Se llama antes de procesar los JSONs. El mod dependiente debe vaciar sus Listas/Mapas aquí.
     */
    protected abstract void clearLocalData();

    /**
     * Procesa un JSON individual. Aquí el mod aplica su lógica de "replace" y lectura de variables.
     */
    protected abstract void processJson(ResourceLocation fileId, JsonObject data);

    /**
     * Se llama cuando todos los JSONs han sido procesados exitosamente.
     */
    protected void onDataLoaded() {}
}