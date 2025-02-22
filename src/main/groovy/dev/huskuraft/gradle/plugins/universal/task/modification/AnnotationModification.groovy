package dev.huskuraft.gradle.plugins.universal.task.modification

import org.gradle.api.tasks.Input
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

/**
 * A modification that updates annotations within class files.
 */
class AnnotationModification extends ClassModification {
    /** The descriptor of the annotation to modify (e.g., "Lcom/example/YourAnnotation;"). */
    @Input
    String descriptor

    /** The field within the annotation to modify. */
    @Input
    String field

    /** The new value to set for the annotation field. */
    @Input
    Object newValue

    @Override
    void modifyClass(ClassWriter classWriter) {
        def visitor = new ClassVisitor(Opcodes.ASM9, classWriter) {
            @Override
            AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                // Check if the annotation matches the target descriptor
                if (descriptor == this.descriptor) {
                    return new AnnotationVisitor(Opcodes.ASM9, super.visitAnnotation(descriptor, visible)) {
                        @Override
                        void visit(String name, Object value) {
                            // Modify the field if it matches the target field
                            if (name == field) {
                                super.visit(name, newValue)
                            } else {
                                super.visit(name, value)
                            }
                        }
                    }
                }
                return super.visitAnnotation(descriptor, visible)
            }
        }

        // Apply the visitor to the class
        def classReader = new ClassReader(classWriter.toByteArray())
        classReader.accept(visitor, 0)
    }
}
