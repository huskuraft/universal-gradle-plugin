package dev.huskuraft.universal.gradle.task.modification.fabric;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.huskuraft.universal.gradle.Mod;
import dev.huskuraft.universal.gradle.task.modification.JsonModification;

import java.util.jar.JarEntry;

/**
 * A modification that updates the `fabric.mod.json` file within a JAR.
 */
public class FabricModJsonPropertyModification extends JsonModification {

    /** The name of the `fabric.mod.json` file. */
    public static final String FILE_NAME = "fabric.mod.json";

    private final Mod mod;

    /**
     * Creates a new `FabricModJsonModification` instance.
     *
     * @param mod The mod information to use for modifications.
     */
    public FabricModJsonPropertyModification(Mod mod) {
        this.mod = mod;
    }

    @Override
    public boolean appliesTo(JarEntry entry) {
        // Only apply to the `fabric.mod.json` file
        return entry.getName().equals(FILE_NAME);
    }

    @Override
    protected void modifyJson(JsonObject jsonObject) {
        // Create a new ordered JsonObject to control field order
        var ordered = new JsonObject();
        
        // 1. Copy schemaVersion first if it exists
        if (jsonObject.has("schemaVersion")) {
            ordered.add("schemaVersion", jsonObject.get("schemaVersion"));
        }
        
        // 2. Add basic mod information
        ordered.addProperty("id", mod.getId());
        ordered.addProperty("version", mod.getVersion());
        ordered.addProperty("name", mod.getName());
        ordered.addProperty("description", mod.getDescription());
        
        // 3. Add authors
        var authorsArray = new JsonArray();
        mod.getAuthors().forEach(authorsArray::add);
        ordered.add("authors", authorsArray);
        
        // 4. Copy contributors if exists
        if (jsonObject.has("contributors")) {
            ordered.add("contributors", jsonObject.get("contributors"));
        }
        
        // 5. Add contact information
        var contactObject = new JsonObject();
        if (jsonObject.has("contact")) {
            contactObject = jsonObject.get("contact").getAsJsonObject();
        }
        contactObject.addProperty("homepage", mod.getPrimaryUrl().toString());
        contactObject.addProperty("issues", mod.getSupportUrl().toString());
        contactObject.addProperty("sources", mod.getSourcesUrl().toString());
        ordered.add("contact", contactObject);
        
        // 6. Add license
        ordered.addProperty("license", mod.getLicense());
        
        // 7. Add icon (RIGHT AFTER license - this is the key ordering)
        ordered.addProperty("icon", "assets/" + mod.getId() + "/icon.png");
        
        // 8. Add environment
        switch (mod.getEnvironment()) {
            case BOTH:
                ordered.addProperty("environment", "*");
                break;
            case CLIENT:
                ordered.addProperty("environment", "client");
                break;
            case SERVER:
                ordered.addProperty("environment", "server");
                break;
        }
        
        // 9. Add entrypoints
        var entrypointsObject = new JsonObject();
        if (jsonObject.has("entrypoints")) {
            entrypointsObject = jsonObject.get("entrypoints").getAsJsonObject();
        }
        
        var mainEntrypointArray = new JsonArray();
        mainEntrypointArray.add(mod.getGroupId() + ".fabric.platform.FabricInitializer");
        entrypointsObject.add("main", mainEntrypointArray);
        
        var clientEntrypointArray = new JsonArray();
        clientEntrypointArray.add(mod.getGroupId() + ".fabric.platform.FabricClientInitializer");
        entrypointsObject.add("client", clientEntrypointArray);
        ordered.add("entrypoints", entrypointsObject);
        
        // 10. Copy jars if exists
        if (jsonObject.has("jars")) {
            ordered.add("jars", jsonObject.get("jars"));
        }
        
        // 11. Add mixins
        var mixinsArray = new JsonArray();
        mixinsArray.add(mod.getId() + ".mixins.json");
        ordered.add("mixins", mixinsArray);
        
        // 12. Add access widener
        ordered.addProperty("accessWidener", mod.getId() + ".accesswidener");
        
        // 13. Copy any remaining fields (depends, suggests, breaks, conflicts, custom, etc.)
        for (var entry : jsonObject.entrySet()) {
            if (!ordered.has(entry.getKey())) {
                ordered.add(entry.getKey(), entry.getValue());
            }
        }
        
        // Replace the original object's contents with the ordered version
        var keys = jsonObject.keySet().toArray(new String[0]);
        for (String key : keys) {
            jsonObject.remove(key);
        }
        for (var entry : ordered.entrySet()) {
            jsonObject.add(entry.getKey(), entry.getValue());
        }
    }
}
