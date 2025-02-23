package dev.huskuraft.gradle.plugins.universal.task.modification;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarEntry;

/**
 * A modification that updates the value of a specific field in a JSON file using GSON.
 */
public abstract class JsonModification implements Modification {

    /** The file extension for JSON files. */
    public static final String JSON_EXTENSION = ".json";

    @Override
    public boolean appliesTo(JarEntry entry) {
        // Only apply to .json files
        return entry.getName().endsWith(JSON_EXTENSION);
    }

    @Override
    public byte[] apply(byte[] input) {
        // Parse the input JSON file
        var jsonElement = JsonParser.parseReader(
                new InputStreamReader(new ByteArrayInputStream(input), StandardCharsets.UTF_8)
        );

        // Ensure the JSON is an object (not an array or primitive)
        if (!jsonElement.isJsonObject()) {
            throw new IllegalArgumentException("Expected a JSON object, but found: " + jsonElement.getClass().getSimpleName());
        }

        // Get the JSON object
        var jsonObject = jsonElement.getAsJsonObject();

        // Apply the modification to the JSON object
        modifyJson(jsonObject);

        // Write the modified JSON to the output stream
        try (var outputStream = new ByteArrayOutputStream();
             var writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            var gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(jsonObject, writer);
            writer.flush();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to write modified JSON to byte array", e);
        }
    }

    /**
     * Abstract method to modify the JSON object.
     *
     * @param jsonObject The JSON object to modify.
     */
    protected abstract void modifyJson(JsonObject jsonObject);
}
