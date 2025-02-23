package dev.huskuraft.gradle.plugins.universal.task.modification.forge;

import dev.huskuraft.gradle.plugins.universal.Mod;
import dev.huskuraft.gradle.plugins.universal.task.modification.PlainTextModification;

import java.util.jar.JarEntry;

/**
 * A modification that updates specific lines in the `mods.toml` file for Forge mods.
 */
public class ForgeModTomlModification extends PlainTextModification {

    /** The name of the `mods.toml` file. */
    public static final String FILE_NAME = "META-INF/mods.toml";

    private final Mod mod;

    /**
     * Creates a new `ForgeModTomlModification` instance.
     *
     * @param mod The mod information to use for modifications.
     */
    public ForgeModTomlModification(Mod mod) {
        this.mod = mod;
    }

    @Override
    public boolean appliesTo(JarEntry entry) {
        // Only apply to the `mods.toml` file
        return entry.getName().equals(FILE_NAME);
    }

    @Override
    protected String modifyText(String content) {
        // Split the content into lines
        var lines = content.split("\n");
        var modifiedContent = new StringBuilder();

        // Iterate through each line and modify it as needed
        for (var line : lines) {
            if (line.trim().startsWith("license=")) {
                line = "license=\"" + mod.getLicense() + "\"";
            } else if (line.trim().startsWith("issueTrackerURL=")) {
                line = "issueTrackerURL=\"" + mod.getIssuesUrl() + "\"";
            } else if (line.trim().startsWith("modId=\"universal\"")) {
                line = "modId=\"" + mod.getId() + "\"";
            } else if (line.trim().startsWith("version=")) {
                line = "version=\"" + mod.getVersion() + "\"";
            } else if (line.trim().startsWith("displayName=")) {
                line = "displayName=\"" + mod.getName() + "\"";
            } else if (line.trim().startsWith("displayURL=")) {
                line = "displayURL=\"" + mod.getDisplayUrl() + "\"";
            } else if (line.trim().startsWith("logoFile=")) {
                line = "logoFile=\"" + "assets/" + mod.getId() + "/icon.png" + "\"";
            } else if (line.trim().startsWith("authors=")) {
                line = "authors=\"" + String.join(",", mod.getAuthors()) + "\"";
            } else if (line.trim().startsWith("displayTest=")) {
                // MATCH_VERSION, IGNORE_SERVER_VERSION, IGNORE_ALL_VERSION, NONE
                line = "displayTest=\"" + "MATCH_VERSION" + "\"";
            } else if (line.trim().startsWith("description=")) {
                line = "description=\"" + mod.getDescription() + "\"";
            } else if (line.trim().startsWith("[[dependencies.universal]]")) {
                line = "[[dependencies." + mod.getId() + "]]";
            } else if (line.trim().startsWith("side=")) {
                line = switch (mod.getEnvironment()) {
                    case BOTH -> "side=\"" + "BOTH" + "\"";
                    case CLIENT -> "side=\"" + "CLIENT" + "\"";
                    case SERVER ->  "side=\"" + "SERVER" + "\"";
                };
            }
            modifiedContent.append(line).append("\n");
        }

        return modifiedContent.toString();
    }
}
