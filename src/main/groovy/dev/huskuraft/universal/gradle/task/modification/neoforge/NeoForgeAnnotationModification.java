package dev.huskuraft.universal.gradle.task.modification.neoforge;

import dev.huskuraft.universal.gradle.Mod;
import dev.huskuraft.universal.gradle.task.modification.AnnotationModification;

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
