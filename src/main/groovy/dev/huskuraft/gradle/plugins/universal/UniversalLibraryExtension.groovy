package dev.huskuraft.gradle.plugins.universal

import dev.huskuraft.gradle.plugins.universal.versioning.Minecraft
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

import javax.inject.Inject

abstract class UniversalLibraryExtension {
    final Property<Minecraft> minecraft
    final Property<Loader> name

    @Inject
    UniversalLibraryExtension(ObjectFactory objectFactory) {
        minecraft = objectFactory.property(Minecraft)
        name = objectFactory.property(Loader)
    }


}
