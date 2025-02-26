package dev.huskuraft.universal.gradle.task.modification.neoforge;

import dev.huskuraft.universal.gradle.Mod;
import dev.huskuraft.universal.gradle.task.modification.forge.ForgeModTomlModification;

import java.util.jar.JarEntry;

/**
 * A modification that updates specific lines in the `mods.toml` file for Forge mods.
 */
public class NeoForgeModTomlModification extends ForgeModTomlModification {

    /** The name of the `neoforge.mods.toml` file. */
    public static final String FILE_NAME = "META-INF/neoforge.mods.toml";

    /**
     * Creates a new `NeoForgeModTomlModification` instance.
     *
     * @param mod The mod information to use for modifications.
     */
    public NeoForgeModTomlModification(Mod mod) {
        super(mod);
    }

    @Override
    public boolean appliesTo(JarEntry entry) {
        // Only apply to the `neoforge.mods.toml` file
        return entry.getName().equals(FILE_NAME);
    }
}
