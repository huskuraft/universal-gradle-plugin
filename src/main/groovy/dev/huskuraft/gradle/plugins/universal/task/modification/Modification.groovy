package dev.huskuraft.gradle.plugins.universal.task.modification

import java.util.zip.ZipEntry

/**
 * Interface for defining modifications to JAR entries.
 */
interface Modification {
    /**
     * Determines if this modification applies to a given JAR entry.
     *
     * @param entry The JAR entry to check.
     * @return True if the modification applies, otherwise false.
     */
    boolean appliesTo(ZipEntry entry)

    /**
     * Applies the modification to the entry
     *
     * @param inputStream The original entry.
     * @param outputStream The modified entry.
     */
    default ZipEntry apply(ZipEntry inputEntry) {
        return inputEntry
    }

    /**
     * Applies the modification to the entry's input stream and writes the result to the output stream.
     *
     * @param inputStream The input stream of the entry.
     * @param outputStream The output stream to write the modified entry.
     */
    OutputStream apply(InputStream inputStream)
}
