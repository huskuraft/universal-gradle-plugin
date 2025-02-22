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

    void includeTarget(String minecraft, List<String> loaders) {
        def target = new UniversalTarget()
        target.minecraft(minecraft)
        target.loaders(loaders)
        includeTargets.add(target)
    }

}
