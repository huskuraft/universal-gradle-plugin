package dev.huskuraft.universal.gradle

import groovy.transform.Internal
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested

import javax.inject.Inject

abstract class UniversalExtension {

    private final ObjectFactory objectFactory

    @Internal
    abstract Property<String> getVersion()

    @Internal
    abstract Property<String> getGroup()

    @Input
    abstract MapProperty<List<String>, List<String>> getTargets()

    @Input
    abstract Property<Boolean> getSplitTargets()

    @Input
    abstract Property<String> getId()

    @Input
    abstract Property<String> getName()

    @Input
    abstract Property<String> getDescription()

    @Input
    abstract ListProperty<String> getAuthors()

    @Input
    abstract Property<String> getLicense()

    @Input
    abstract Property<String> getEnvironment()

    @Input
    abstract Property<URI> getPrimaryUrl()

    @Input
    abstract Property<URI> getSourcesUrl()

    @Input
    abstract Property<URI> getSupportUrl()

    @Input
    abstract Property<String> getChangelog()

    @Input
    abstract Property<String> getChangelogFormat()

    @Nested
    abstract Modrinth getModrinth()

    @Nested
    @Internal
    abstract CurseForge getCurseforge()

    void splitTargets() {
        getSplitTargets().set(true)
    }

    void targets(Map<List<String>, List<String>> targets) {
        getTargets().set(targets)
    }

    void modrinth(Action<? super Modrinth> action) {
        action.execute(getModrinth())
    }

    void curseforge(Action<? super CurseForge> action) {
        action.execute(getCurseforge())
    }

    @Inject
    UniversalExtension(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory
    }

    abstract static class Modrinth {
        @Input
        abstract Property<String> getId()

        @Input
        abstract Property<String> getToken()
    }

    abstract static class CurseForge {
        @Input
        abstract Property<String> getId()

        @Input
        abstract Property<String> getToken()
    }
}
