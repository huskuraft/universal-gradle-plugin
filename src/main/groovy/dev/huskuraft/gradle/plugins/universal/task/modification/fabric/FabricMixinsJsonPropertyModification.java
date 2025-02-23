package dev.huskuraft.gradle.plugins.universal.task.modification.fabric;

import com.google.gson.JsonObject;
import dev.huskuraft.gradle.plugins.universal.Mod;
import dev.huskuraft.gradle.plugins.universal.task.modification.JsonModification;

import java.util.jar.JarEntry;

/**
 * A modification that updates the `mixins.json` file within a JAR.
 */
public class FabricMixinsJsonPropertyModification extends JsonModification {

    /** The name of the `mixins.json` file. */
    public static final String FILE_NAME = "mixins.json";

    private final Mod mod;

    /**
     * Creates a new `FabricMixinsJsonPropertyModification` instance.
     *
     * @param mod The mod information to use for modifications.
     */
    public FabricMixinsJsonPropertyModification(Mod mod) {
        this.mod = mod;
    }

    @Override
    public boolean appliesTo(JarEntry entry) {
        // Only apply to the `mixins.json` file
        return entry.getName().endsWith(FILE_NAME);
    }

    @Override
    protected void modifyJson(JsonObject jsonObject) {
        jsonObject.addProperty("package", mod.getGroupId() + ".fabric.mixin");
        jsonObject.addProperty("refmap", mod.getId() + ".refmap.json");
    }
}
