package dev.huskuraft.universal.gradle.task.modification;

import org.gradle.api.tasks.Input;
import org.objectweb.asm.*;

/**
 * A modification that updates annotations within class files.
 */
public class AnnotationModification extends ClassModification {

    /** The descriptor of the annotation to modify (e.g., "Lcom/example/YourAnnotation;"). */
    @Input
    private String descriptor;

    /** The field within the annotation to modify. */
    @Input
    private String field;

    /** The new value to set for the annotation field. */
    @Input
    private Object newValue;

    /**
     * Gets the descriptor of the annotation to modify.
     *
     * @return The annotation descriptor.
     */
    public String getDescriptor() {
        return descriptor;
    }

    /**
     * Sets the descriptor of the annotation to modify.
     *
     * @param descriptor The annotation descriptor.
     */
    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * Gets the field within the annotation to modify.
     *
     * @return The annotation field.
     */
    public String getField() {
        return field;
    }

    /**
     * Sets the field within the annotation to modify.
     *
     * @param field The annotation field.
     */
    public void setField(String field) {
        this.field = field;
    }

    /**
     * Gets the new value to set for the annotation field.
     *
     * @return The new value.
     */
    public Object getNewValue() {
        return newValue;
    }

    /**
     * Sets the new value to set for the annotation field.
     *
     * @param newValue The new value.
     */
    public void setNewValue(Object newValue) {
        this.newValue = newValue;
    }

    @Override
    public ClassVisitor createVisitor(ClassWriter classWriter) {
        return new ClassVisitor(Opcodes.ASM9, classWriter) {
            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                // Custom logic to modify annotations
                if (descriptor.equals(AnnotationModification.this.descriptor)) {
                    return new AnnotationVisitor(Opcodes.ASM9, super.visitAnnotation(descriptor, visible)) {
                        @Override
                        public void visit(String name, Object value) {
                            if (name.equals(AnnotationModification.this.field)) {
                                // Modify the annotation field value
                                super.visit(name, AnnotationModification.this.newValue);
                            } else {
                                super.visit(name, value);
                            }
                        }
                    };
                }
                return super.visitAnnotation(descriptor, visible);
            }
        };
    }
}
