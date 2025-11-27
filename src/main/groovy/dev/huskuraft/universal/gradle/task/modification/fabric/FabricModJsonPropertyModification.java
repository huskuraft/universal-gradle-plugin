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
        // Update basic mod information
        jsonObject.addProperty("id", mod.getId());
        jsonObject.addProperty("name", mod.getName());
        jsonObject.addProperty("version", mod.getVersion());
        jsonObject.addProperty("description", mod.getDescription());

        // Update authors
        var authorsArray = new JsonArray();
        mod.getAuthors().forEach(authorsArray::add);
        jsonObject.add("authors", authorsArray);

        // Update contact information
        var contactObject = jsonObject.get("contact").getAsJsonObject();
        contactObject.addProperty("homepage", mod.getPrimaryUrl().toString());
        contactObject.addProperty("issues", mod.getSupportUrl().toString());
        contactObject.addProperty("sources", mod.getSourcesUrl().toString());
        jsonObject.add("contact", contactObject);

        // Update license
        jsonObject.addProperty("license", mod.getLicense());

        // Update icon
        jsonObject.addProperty("icon", "assets/" + mod.getId() + "/icon.png");

        // Update environment
        switch (mod.getEnvironment()) {
            case BOTH:
                jsonObject.addProperty("environment", "*");
                break;
            case CLIENT:
                jsonObject.addProperty("environment", "client");
                break;
            case SERVER:
                jsonObject.addProperty("environment", "server");
                break;
        }

        // Update entrypoints
        var entrypointsObject = jsonObject.get("entrypoints").getAsJsonObject();

        var mainEntrypointArray = new JsonArray();
        mainEntrypointArray.add(mod.getGroupId() + ".fabric.platform.FabricInitializer");
        entrypointsObject.add("main", mainEntrypointArray);

        var clientEntrypointArray = new JsonArray();
        clientEntrypointArray.add(mod.getGroupId() + ".fabric.platform.FabricClientInitializer");
        entrypointsObject.add("client", clientEntrypointArray);

        // Update mixins
        var mixinsArray = new JsonArray();
        mixinsArray.add(mod.getId() + ".mixins.json");
        jsonObject.add("mixins", mixinsArray);

        // Update access widener
        jsonObject.addProperty("accessWidener", mod.getId() + ".accesswidener");
    }
}
