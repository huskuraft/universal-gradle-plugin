package dev.huskuraft.gradle.plugins.universal

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

import javax.inject.Inject

abstract class UniversalExtension {
    final ListProperty<UniversalTarget> includeTargets

    @Inject
    UniversalExtension(ObjectFactory objectFactory) {
        includeTargets = objectFactory.listProperty(UniversalTarget)
    }

    void includeTarget(Action<UniversalTarget> action) {
        def target = new UniversalTarget()
        action.execute(target)
        includeTargets.add(target)
    }

    void includePrimaryTargets() {
        includeTargets.set(UniversalTarget.primaryTargets().toList())
    }

    void includeAllTargets() {
        includeTargets.set(UniversalTarget.allTargets().toList())
    }

}
