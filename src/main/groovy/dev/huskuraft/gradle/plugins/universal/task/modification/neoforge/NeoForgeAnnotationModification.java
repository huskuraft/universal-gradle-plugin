package dev.huskuraft.gradle.plugins.universal.task.modification.neoforge;

import dev.huskuraft.gradle.plugins.universal.Mod;
import dev.huskuraft.gradle.plugins.universal.task.modification.AnnotationModification;

/**
 * A modification that updates NeoForge annotations within class files.
 */
public class NeoForgeAnnotationModification extends AnnotationModification {

    /**
     * Creates a new `NeoForgeAnnotationModification` instance.
     *
     * @param mod The mod information to use for modifications.
     */
    public NeoForgeAnnotationModification(Mod mod) {
        this.setDescriptor("Lnet/neoforged/fml/common/Mod;");
        this.setField("value");
        this.setNewValue(mod.getId());
    }
}
