package dev.huskuraft.gradle.plugins.universal.task;

import dev.huskuraft.gradle.plugins.universal.task.modification.AnnotationModification;
import dev.huskuraft.gradle.plugins.universal.task.modification.Modification;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * A Gradle task that modifies entries within a JAR file.
 * This task supports applying custom modifications to specific entries, such as class files or annotations.
 * Caching is disabled by default to ensure modifications are always applied.
 */
@DisableCachingByDefault
public class JarModificationTask extends DefaultTask {

    @Input
    public List<Modification> getModifications() {
        return modifications;
    }

    /**
     * Adds a modification to the task.
     *
     * @param modification The modification to add.
     */
    public void modification(Modification modification) {
        modifications.add(modification);
    }

    /**
     * Adds an annotation modification using an Action for configuration.
     *
     * @param action The action to configure the AnnotationModification.
     */
    public void annotation(Action<AnnotationModification> action) {
        var modification = new AnnotationModification();
        action.execute(modification);
        modifications.add(modification);
    }

    @TaskAction
    public void doTask() throws IOException {
        // Replace the input file in place
        var inputJar = inputFile.get().getAsFile();
        var tempOutputJar = new File(getTemporaryDir(), inputJar.getName());

        // Read the input JAR file
        try (var jarInput = new JarFile(inputJar);
             var jarOutput = new JarOutputStream(new FileOutputStream(tempOutputJar))) {

            jarInput.entries().asIterator().forEachRemaining(entry -> {
                try {
                    // Find modifications that apply to this entry
                    var applicableModifications = getModifications().stream()
                            .filter(modification -> modification.appliesTo(entry))
                            .toList();

                    if (!applicableModifications.isEmpty()) {
                        // Read the entry content into a byte array
                        byte[] entryContent;
                        try (InputStream inputStream = jarInput.getInputStream(entry)) {
                            entryContent = inputStream.readAllBytes();
                        }

                        // Apply modifications sequentially
                        var modifiedEntry = entry; // Start with the original entry
                        for (var modification : applicableModifications) {
                            modifiedEntry = modification.apply(modifiedEntry);
                            entryContent = modification.apply(entryContent);
                        }

                        // Write the modified entry to the output JAR
                        jarOutput.putNextEntry(modifiedEntry);
                        jarOutput.write(entryContent);
                        jarOutput.closeEntry();
                    } else {
                        // Copy the entry as-is
                        try (InputStream inputStream = jarInput.getInputStream(entry)) {
                            var outputEntry = new JarEntry(entry.getName());
                            jarOutput.putNextEntry(outputEntry);
                            jarOutput.write(inputStream.readAllBytes());
                            jarOutput.closeEntry();
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed to process JAR entry: " + entry.getName(), e);
                }
            });
        }

        // Replace the original JAR with the modified one
        if (inputJar.delete()) {
            if (tempOutputJar.renameTo(inputJar)) {
                return;
            }
        }

        throw new IllegalStateException("Failed to replace the original JAR file.");
    }

    public Provider<RegularFile> getInputFile() {
        return inputFile;
    }

    public void setInputFile(Provider<RegularFile> inputFile) {
        this.inputFile = inputFile;
    }

    /**
     * The input JAR file to be modified.
     */
    @InputFile
    private Provider<RegularFile> inputFile;

    /**
     * List of modifications to apply to the JAR file.
     */
    private final List<Modification> modifications = new ArrayList<>();
}
