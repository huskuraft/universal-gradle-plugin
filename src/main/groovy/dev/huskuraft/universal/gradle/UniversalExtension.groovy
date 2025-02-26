package dev.huskuraft.universal.gradle

import org.gradle.api.model.ObjectFactory

import javax.inject.Inject

abstract class UniversalExtension {
    @Inject
    UniversalExtension(ObjectFactory objectFactory) {
    }

}
