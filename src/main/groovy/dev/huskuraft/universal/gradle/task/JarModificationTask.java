package dev.huskuraft.universal.gradle.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFile;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.WorkResult;
import org.gradle.api.tasks.WorkResults;
import org.gradle.work.DisableCachingByDefault;

import dev.huskuraft.universal.gradle.task.modification.AnnotationModification;
import dev.huskuraft.universal.gradle.task.modification.Modification;

/**
 * A Gradle task that modifies entries within a JAR file.
 * This task supports applying custom modifications to specific entries, such as class files or annotations.
 * Caching is disabled by default to ensure modifications are always applied.
 */
@DisableCachingByDefault
public class JarModificationTask extends DefaultTask {

    @InputFile
    private Provider<RegularFile> inputFile;
    @OutputFile
    private Provider<RegularFile> outputFile;
    @Nested
    private final ListProperty<Modification> modifications = getObjectFactory().listProperty(Modification.class);

    public List<Modification> getModifications() {
        return modifications.get();
    }

    public void modification(Modification modification) {
        modifications.add(modification);
    }

    public void annotation(Action<AnnotationModification> action) {
        var modification = new AnnotationModification();
        action.execute(modification);
        modifications.add(modification);
    }

    public Provider<RegularFile> getInputFile() {
        return inputFile;
    }

    public void setInputFile(Provider<RegularFile> inputFile) {
        this.inputFile = inputFile;
    }

    public Provider<RegularFile> getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(Provider<RegularFile> outputFile) {
        this.outputFile = outputFile;
    }

    @Inject
    protected ObjectFactory getObjectFactory() {
        throw new UnsupportedOperationException();
    }

    @TaskAction
    public void execute() throws IOException {
        var action = new JarModificationAction(inputFile.get(), outputFile.get(), getModifications());
        action.execute();
    }

    public static class JarModificationAction {

        private final RegularFile inputFile;
        private final RegularFile outputFile;
        private final List<Modification> modifications;

        public JarModificationAction(RegularFile inputFile, RegularFile outputFile, List<Modification> modifications) {
            this.inputFile = inputFile;
            this.outputFile = outputFile;
            this.modifications = modifications;
        }

        public WorkResult execute() {
            try {
                // Replace the input file in place
                var inputJar = inputFile.getAsFile();
                var outputJar = outputFile.getAsFile();
                var tempOutputJar = new File(inputJar.getParentFile(), inputJar.getName() + ".tmp");

                // Read the input JAR file
                try (var jarInput = new JarFile(inputJar);
                     var jarOutput = new JarOutputStream(new FileOutputStream(tempOutputJar))) {

                    jarInput.entries().asIterator().forEachRemaining(entry -> {
                        try {
                            // Find modifications that apply to this entry
                            var applicableModifications = modifications.stream()
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
                    if (tempOutputJar.renameTo(outputJar)) {
                        return WorkResults.didWork(true);
                    }
                }

                throw new IllegalStateException("Failed to replace the original JAR file.");
            } catch (IOException e) {
                throw new RuntimeException("Failed to modify JAR file", e);
            }
        }
    }


}
