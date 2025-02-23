package dev.huskuraft.gradle.plugins.universal.task.modification

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser

import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry

/**
 * A modification that updates the value of a specific field in a JSON file using GSON.
 */
abstract class JsonModification implements Modification {

    static String JSON_EXTENSION = ".json"

    @Override
    boolean appliesTo(ZipEntry entry) {
        return entry.name.endsWith(JSON_EXTENSION) // Only apply to .json files
    }

    @Override
    void apply(InputStream inputStream, OutputStream outputStream) {
        // Parse the input JSON file
        JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
        println("Applying JSON modification to: ${jsonElement}")

        // Ensure the JSON is an object (not an array or primitive)
        if (!jsonElement.isJsonObject()) {
            throw new IllegalArgumentException("Expected a JSON object, but found: ${jsonElement.class.simpleName}")
        }

        // Get the JSON object
        JsonObject jsonObject = jsonElement.asJsonObject

        // Apply the modification to the JSON object
        modifyJson(jsonObject)

        // Write the modified JSON to the output stream
        outputStream.withWriter(StandardCharsets.UTF_8.name()) { writer ->
            def gson = new GsonBuilder().setPrettyPrinting().create()
            gson.toJson(jsonObject, writer)
        }
    }

    /**
     * Abstract method to modify the JSON object.
     *
     * @param jsonObject The JSON object to modify.
     */
    protected abstract void modifyJson(JsonObject jsonObject)
}
