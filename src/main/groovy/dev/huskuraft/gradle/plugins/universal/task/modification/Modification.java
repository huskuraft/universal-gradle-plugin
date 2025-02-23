package dev.huskuraft.gradle.plugins.universal.task.modification;

import java.util.jar.JarEntry;

/**
 * Interface for defining modifications to JAR entries.
 */
public interface Modification {
    /**
     * Determines if this modification applies to a given JAR entry.
     *
     * @param entry The JAR entry to check.
     * @return True if the modification applies, otherwise false.
     */
    boolean appliesTo(JarEntry entry);

    /**
     * Applies the modification to the entry.
     *
     * @param inputEntry The original entry.
     * @return The modified entry.
     */
    default JarEntry apply(JarEntry inputEntry) {
        return inputEntry;
    }

    /**
     * Applies the modification to the entry's content.
     *
     * @param input The input byte array of the entry.
     * @return The modified byte array.
     */
    byte[] apply(byte[] input);
}
