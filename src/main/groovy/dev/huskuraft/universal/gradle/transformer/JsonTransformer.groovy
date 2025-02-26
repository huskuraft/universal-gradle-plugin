package dev.huskuraft.universal.gradle.transformer

import com.github.jengelman.gradle.plugins.shadow.transformers.CacheableTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.Transformer
import com.github.jengelman.gradle.plugins.shadow.transformers.TransformerContext
import com.google.gson.*
import org.apache.tools.zip.ZipEntry
import org.apache.tools.zip.ZipOutputStream
import org.gradle.api.file.FileTreeElement
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

@CacheableTransformer
class JsonTransformer implements Transformer {
    static final GSON = new Gson()
    static final LOGGER = Logging.getLogger(JsonTransformer.class)

    @Optional
    @Input
    String resource

    private JsonElement json

    @Override
    boolean canTransformResource(FileTreeElement element) {
        def path = element.relativePath.pathString
        return (resource != null && resource.equalsIgnoreCase(path))
    }

    @Override
    void transform(TransformerContext context) {
        JsonElement j
        try {
            j = JsonParser.parseReader(new InputStreamReader(context.is, "UTF-8"))
        } catch (Exception e) {
            throw new RuntimeException("error on processing json", e)
        }

        json = (json == null) ? j : mergeJson(json, j)
    }

    @Override
    boolean hasTransformedResource() {
        return json != null
    }

    @Override
    void modifyOutputStream(ZipOutputStream os, boolean preserveFileTimestamps) {
        ZipEntry entry = new ZipEntry(resource)
        entry.time = TransformerContext.getEntryTimestamp(preserveFileTimestamps, entry.time)
        os.putNextEntry(entry)
        os.write(GSON.toJson(json).getBytes())

        json = null
    }

    /**
     * <table>
     *     <tr>
     *         <td>{@code lhs}</td> <td>{@code rhs}</td> <td>{@code return}</td>
     *     </tr>
     *     <tr>
     *         <td>Any</td> <td>{@code JsonNull}</td> <td>{@code lhs}</td>
     *     </tr>
     *     <tr>
     *         <td>{@code JsonNull}</td> <td>Any</td> <td>{@code rhs}</td>
     *     </tr>
     *     <tr>
     *         <td>{@code JsonArray}</td> <td>{@code JsonArray}</td> <td>concatenation</td>
     *     </tr>
     *     <tr>
     *         <td>{@code JsonObject}</td> <td>{@code JsonObject}</td> <td>merge for each key</td>
     *     </tr>
     *     <tr>
     *         <td>{@code JsonPrimitive}</td> <td>{@code JsonPrimitive}</td>
     *         <td>return lhs if {@code lhs.equals(rhs)}, error otherwise</td>
     *     </tr>
     *     <tr>
     *         <td colspan="2">Other</td> <td>error</td>
     *     </tr>
     * </table>
     * @param lhs a {@code JsonElement}
     * @param rhs a {@code JsonElement}
     * @param id used for logging purpose only
     * @return the merged {@code JsonElement}
     */
    static JsonElement mergeJson(JsonElement lhs, JsonElement rhs, String id = "") {
        if (rhs == null || rhs instanceof JsonNull) {
            return lhs
        } else if (lhs == null || lhs instanceof JsonNull) {
            return rhs
        } else if (lhs instanceof JsonArray && rhs instanceof JsonArray) {
            return mergeJsonArray(lhs as JsonArray, rhs as JsonArray, id)
        } else if (lhs instanceof JsonObject && rhs instanceof JsonObject) {
            return mergeJsonObject(lhs as JsonObject, rhs as JsonObject, id)
        } else if (lhs instanceof JsonPrimitive && rhs instanceof JsonPrimitive) {
            return mergeJsonPrimitive(lhs as JsonPrimitive, rhs as JsonPrimitive, id)
        } else {
            LOGGER.warn("conflicts for property {} detected, {} & {}",
                id, lhs.toString(), rhs.toString())
            return lhs
        }
    }

    static JsonPrimitive mergeJsonPrimitive(JsonPrimitive lhs, JsonPrimitive rhs, String id) {
        / In Groovy, {@code a == b} is equivalent to {@code a.equals(b)} /
        if (lhs != rhs) {
            LOGGER.warn("conflicts for property {} detected, {} & {}",
                id, lhs.toString(), rhs.toString())
        }
        return lhs
    }

    static JsonObject mergeJsonObject(JsonObject lhs, JsonObject rhs, String id) {
        JsonObject object = new JsonObject()

        Set<String> properties = new HashSet<>()
        properties.addAll(lhs.keySet())
        properties.addAll(rhs.keySet())
        for (String property : properties) {
            object.add(property,
                mergeJson(lhs.get(property), rhs.get(property), id + ":" + property))
        }

        return object
    }

    static JsonArray mergeJsonArray(JsonArray lhs, JsonArray rhs, String id) {
        JsonArray array = new JsonArray()

        array.addAll(lhs)
        array.addAll(rhs)

        return array
    }
}
