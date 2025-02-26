package dev.huskuraft.universal.gradle.task.modification.fabric;

import dev.huskuraft.universal.gradle.Mod;
import dev.huskuraft.universal.gradle.task.modification.RenameModification;

import java.util.regex.Pattern;

/**
 * A modification that renames the `fabric.accesswidener` file to `$mod_id.accesswidener`.
 */
public class FabricAccessWidenerRenameModification extends RenameModification {

    private static final String FABRIC_ACCESSWIDENER = "fabric.accesswidener";

    /**
     * Creates a new `FabricAccessWidenerRenameModification` instance.
     *
     * @param mod The mod object containing the `id` to use for renaming.
     */
    public FabricAccessWidenerRenameModification(Mod mod) {
        super(Pattern.compile(FABRIC_ACCESSWIDENER), mod.getId() + ".accesswidener");
    }
}
