package dev.huskuraft.universal.gradle.task.modification;

import org.gradle.api.tasks.Input;

import java.util.jar.JarEntry;
import java.util.regex.Pattern;

/**
 * A modification that renames a JAR entry based on a pattern.
 */
public class RenameModification implements Modification {

    @Input
    private final Pattern pattern;

    @Input
    private final String replacement;

    /**
     * Creates a new `RenameModification` instance.
     *
     * @param pattern     The regex pattern to match the entry name.
     * @param replacement The replacement string for the matched pattern.
     */
    public RenameModification(Pattern pattern, String replacement) {
        this.pattern = pattern;
        this.replacement = replacement;
    }

    @Override
    public boolean appliesTo(JarEntry entry) {
        // Apply to all entries that match the pattern
        return pattern.matcher(entry.getName()).matches();
    }

    @Override
    public JarEntry apply(JarEntry inputEntry) {
        // Rename the entry by replacing the matched pattern
        String newName = pattern.matcher(inputEntry.getName()).replaceAll(replacement);
        return new JarEntry(newName);
    }

    @Override
    public byte[] apply(byte[] input) {
        // Return the input as-is (no modification to the content)
        return input;
    }
}
