package dev.huskuraft.universal.gradle.task.modification.fabric;

import dev.huskuraft.universal.gradle.Mod;
import dev.huskuraft.universal.gradle.task.modification.RenameModification;

import java.util.regex.Pattern;

/**
 * A modification that renames the `fabric.refmap.json` file to `$mod_id.refmap.json`.
 */
public class FabricRefmapJsonRenameModification extends RenameModification {

    private static final String FABRIC_REFMAP_JSON = "fabric.refmap.json";

    /**
     * Creates a new `FabricRefmapJsonRenameModification` instance.
     *
     * @param mod The mod object containing the `id` to use for renaming.
     */
    public FabricRefmapJsonRenameModification(Mod mod) {
        super(Pattern.compile(FABRIC_REFMAP_JSON), mod.getId() + ".refmap.json");
    }
}
