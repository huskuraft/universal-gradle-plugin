package dev.huskuraft.gradle.plugins.universal.task.modification.forge;

import dev.huskuraft.gradle.plugins.universal.Mod;
import dev.huskuraft.gradle.plugins.universal.task.modification.AnnotationModification;

/**
 * A modification that updates Forge annotations within class files.
 */
public class ForgeAnnotationModification extends AnnotationModification {

    /**
     * Creates a new `ForgeAnnotationModification` instance.
     *
     * @param mod The mod information to use for modifications.
     */
    public ForgeAnnotationModification(Mod mod) {
        this.setDescriptor("Lnet/minecraftforge/fml/common/Mod;");
        this.setField("value");
        this.setNewValue(mod.getId());
    }
}
