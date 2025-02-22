package dev.huskuraft.gradle.plugins.universal.task.modification

import org.gradle.api.tasks.Input
import org.objectweb.asm.*

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
    ClassVisitor createVisitor(ClassWriter classWriter) {
        return new ClassVisitor(Opcodes.ASM9, classWriter) {
            @Override
            AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                // Custom logic to modify annotations
                if (descriptor == AnnotationModification.this.descriptor) {
                    return new AnnotationVisitor(Opcodes.ASM9, super.visitAnnotation(descriptor, visible)) {
                        @Override
                        void visit(String name, Object value) {
                            if (name == AnnotationModification.this.field) {
                                // Modify the annotation field value
                                super.visit(name, AnnotationModification.this.newValue)
                            } else {
                                super.visit(name, value)
                            }
                        }
                    }
                }
                return super.visitAnnotation(descriptor, visible)
            }
        }
    }
}
