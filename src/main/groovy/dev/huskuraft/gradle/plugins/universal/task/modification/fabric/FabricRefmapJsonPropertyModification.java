package dev.huskuraft.gradle.plugins.universal.task.modification.fabric;

import com.google.gson.JsonObject;
import dev.huskuraft.gradle.plugins.universal.Mod;
import dev.huskuraft.gradle.plugins.universal.task.modification.JsonModification;

import java.util.jar.JarEntry;

/**
 * A modification that updates the `refmap.json` file within a JAR, replacing all properties with the groupId from the mod.
 */
public class FabricRefmapJsonPropertyModification extends JsonModification {

    /** The name of the `refmap.json` file. */
    public static final String FILE_NAME = "refmap.json";

    private final Mod mod;

    /**
     * Creates a new `FabricRefmapJsonPropertyModification` instance.
     *
     * @param mod The mod information to use for modifications.
     */
    public FabricRefmapJsonPropertyModification(Mod mod) {
        this.mod = mod;
    }

    @Override
    public boolean appliesTo(JarEntry entry) {
        // Only apply to the `refmap.json` file
        return entry.getName().endsWith(FILE_NAME);
    }

    @Override
    protected void modifyJson(JsonObject jsonObject) {
        // Replace all properties with the groupId from the mod
        var groupIdPath = mod.getGroupId().replace('.', '/');

        // Update the "mappings" section
        if (jsonObject.has("mappings")) {
            var mappings = jsonObject.getAsJsonObject("mappings");
            mappings.keySet().stream().toList().forEach(key -> {
                String newKey = key.replace("dev/huskuraft/universal", groupIdPath);
                mappings.add(newKey, mappings.remove(key));
            });
        }

        // Update the "data" section
        if (jsonObject.has("data")) {
            var data = jsonObject.getAsJsonObject("data");
            if (data.has("named:intermediary")) {
                var namedIntermediary = data.getAsJsonObject("named:intermediary");
                namedIntermediary.keySet().stream().toList().forEach(key -> {
                    String newKey = key.replaceFirst("dev/huskuraft/universal", groupIdPath);
                    namedIntermediary.add(newKey, namedIntermediary.remove(key));
                });
            }
        }
    }
}
