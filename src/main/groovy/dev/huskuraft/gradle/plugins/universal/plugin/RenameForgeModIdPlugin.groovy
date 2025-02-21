package dev.huskuraft.gradle.plugins.universal.plugin

import net.bytebuddy.build.Plugin
import net.bytebuddy.description.annotation.AnnotationDescription
import net.bytebuddy.description.type.TypeDescription
import net.bytebuddy.dynamic.ClassFileLocator
import net.bytebuddy.dynamic.DynamicType

class RenameForgeModIdPlugin implements Plugin {

    private final String modId

    RenameForgeModIdPlugin(String modId) {
        this.modId = modId
        println(modId)
    }

    @Override
    boolean matches(TypeDescription target) {
        return false
    }

    @Override
    DynamicType.Builder<?> apply(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassFileLocator classFileLocator) {
        AnnotationDescription annotation = typeDescription.getDeclaredAnnotations()
            .ofType(Mod.class)
        AnnotationDescription newAnnotation = AnnotationDescription.Builder.ofType(Mod.class)
            .define("value", "transformed_string")
            .build()

        return null
    }

    @Override
    void close() {
    }
}
