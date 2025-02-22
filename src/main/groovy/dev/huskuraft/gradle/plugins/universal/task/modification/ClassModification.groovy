package dev.huskuraft.gradle.plugins.universal.task.modification


import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter

import java.util.zip.ZipEntry

/**
 * Abstract class for modifications that apply to class files within the JAR.
 */
abstract class ClassModification implements Modification {
    @Override
    boolean appliesTo(ZipEntry entry) {
        return entry.name.endsWith('.class') // Only apply to .class files
    }

    @Override
    void apply(InputStream inputStream, OutputStream outputStream) {
        // Read the class bytes
        def classBytes = inputStream.bytes

        // Initialize ClassReader and ClassWriter
        def classReader = new ClassReader(classBytes)
        def classWriter = new ClassWriter(classReader, 0)

        // Apply the modification
        modifyClass(classWriter)

        // Write the modified class to the output stream
        def modifiedClassBytes = classWriter.toByteArray()
        outputStream.write(modifiedClassBytes)
    }

    /**
     * Abstract method to modify the class using the provided ClassWriter.
     *
     * @param classWriter The ClassWriter to use for modifications.
     */
    abstract void modifyClass(ClassWriter classWriter)
}
