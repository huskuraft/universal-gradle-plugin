package dev.huskuraft.gradle.plugins.universal.task.modification;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.util.jar.JarEntry;

/**
 * Abstract class for modifications that apply to class files within the JAR.
 */
public abstract class ClassModification implements Modification {

    /** The file extension for Java class files. */
    public static final String CLASS_EXTENSION = ".class";

    @Override
    public boolean appliesTo(JarEntry entry) {
        // Only apply to .class files
        return entry.getName().endsWith(CLASS_EXTENSION);
    }

    @Override
    public byte[] apply(byte[] input) {
        // Initialize ClassReader and ClassWriter
        var classReader = new ClassReader(input);
        var classWriter = new ClassWriter(classReader, 0);

        // Apply the modification
        var visitor = createVisitor(classWriter);
        classReader.accept(visitor, 0);

        return classWriter.toByteArray();
    }

    /**
     * Abstract method to create a ClassVisitor to modify the class.
     *
     * @param classWriter The ClassWriter to use for modifications.
     * @return A ClassVisitor that applies the desired modifications.
     */
    public abstract ClassVisitor createVisitor(ClassWriter classWriter);
}
