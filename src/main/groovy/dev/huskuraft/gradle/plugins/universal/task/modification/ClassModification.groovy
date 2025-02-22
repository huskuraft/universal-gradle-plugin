package dev.huskuraft.gradle.plugins.universal.task.modification


import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
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
        // Initialize ClassReader and ClassWriter
        def classReader = new ClassReader(inputStream)
        def classWriter = new ClassWriter(classReader, 0)

        // Apply the modification
        def visitor = createVisitor(classWriter)
        classReader.accept(visitor, 0)

        // Write the modified class to the output stream
        def modifiedClassBytes = classWriter.toByteArray()
        outputStream.write(modifiedClassBytes)
    }

    /**
     * Abstract method to create a ClassVisitor to modify the class.
     *
     * @param classWriter The ClassWriter to use for modifications.
     */
    abstract ClassVisitor createVisitor(ClassWriter classWriter)
}
