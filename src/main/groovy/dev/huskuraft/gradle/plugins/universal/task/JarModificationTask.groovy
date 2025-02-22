package dev.huskuraft.gradle.plugins.universal.task

import dev.huskuraft.gradle.plugins.universal.task.modification.AnnotationModification
import dev.huskuraft.gradle.plugins.universal.task.modification.Modification
import org.gradle.api.Action
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.InputFile
import org.gradle.work.DisableCachingByDefault

import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * A Gradle task that modifies entries within a JAR file.
 * This task supports applying custom modifications to specific entries, such as class files or annotations.
 * Caching is disabled by default to ensure modifications are always applied.
 */
@DisableCachingByDefault
class JarModificationTask extends Copy {

    /** The input JAR file to be modified. */
    @InputFile
    Provider<RegularFile> inputFile

    /** List of modifications to apply to the JAR file. */
    private final List<Modification> modifications = []


    /**
     * Adds a modification to the task.
     *
     * @param modification The modification to add.
     */
    void modification(Modification modification) {
        modifications.add(modification)
    }

    /**
     * Adds an annotation modification using an Action for configuration.
     *
     * @param action The action to configure the AnnotationModification.
     */
    void annotation(Action<AnnotationModification> action) {
        def modification = new AnnotationModification()
        action.execute(modification)
        modifications.add(modification)
    }

    @Override
    protected void copy() {
        // Replace the input file in place
        def inputJar = inputFile.get().asFile
        def tempOutputJar = new File(inputJar.parentFile, "${inputJar.name}.tmp")

        // Read the input JAR file
        def jarInput = new JarFile(inputJar)
        def jarOutput = new JarOutputStream(new FileOutputStream(tempOutputJar))

        jarInput.entries().each { entry ->
            def inputStream = jarInput.getInputStream(entry)
            def outputEntry = new ZipEntry(entry.name)

            // Find modifications that apply to this entry
            def applicableModifications = modifications.findAll { it.appliesTo(entry) }

            if (applicableModifications) {
                // Apply modifications
                def outputStream = new ByteArrayOutputStream()
                applicableModifications.each { modification ->
                    modification.apply(inputStream, outputStream)
                }
                // Write the modified entry to the output JAR
                jarOutput.putNextEntry(outputEntry)
                jarOutput.write(outputStream.toByteArray())
                jarOutput.closeEntry()
            } else {
                // Copy the entry as-is
                jarOutput.putNextEntry(outputEntry)
                jarOutput.write(inputStream.bytes)
                jarOutput.closeEntry()
            }
            inputStream.close()
        }

        jarInput.close()
        jarOutput.close()

        // Replace the original JAR with the modified one
        if (inputJar.delete()) {
            tempOutputJar.renameTo(inputJar)
            println("Modified JAR saved: ${inputJar.absolutePath}")
        } else {
            throw new IllegalStateException("Failed to replace the original JAR file.")
        }
    }
}
