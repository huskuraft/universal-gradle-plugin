package dev.huskuraft.gradle.plugins.universal.task.modification.fabric;

import dev.huskuraft.gradle.plugins.universal.Mod;
import dev.huskuraft.gradle.plugins.universal.task.modification.RenameModification;

import java.util.regex.Pattern;

/**
 * A modification that renames the `fabric.mixins.json` file to `$mod_id.mixins.json`.
 */
public class FabricMixinsJsonRenameModification extends RenameModification {

    private static final String FABRIC_MIXIN_JSON = "fabric.mixins.json";

    /**
     * Creates a new `FabricMixinJsonRenameModification` instance.
     *
     * @param mod The mod object containing the `id` to use for renaming.
     */
    public FabricMixinsJsonRenameModification(Mod mod) {
        super(Pattern.compile(FABRIC_MIXIN_JSON), mod.getId() + ".mixins.json");
    }
}
