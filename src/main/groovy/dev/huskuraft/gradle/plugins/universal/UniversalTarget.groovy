package dev.huskuraft.gradle.plugins.universal

import dev.huskuraft.gradle.plugins.universal.versioning.Minecraft
import dev.huskuraft.gradle.plugins.universal.versioning.VersionResolver
import groovy.transform.Internal

class UniversalTarget {

    Minecraft minecraft
    Loader[] loaders

    void loaders(String... loaders) { this.loaders = loaders.collect { Loader.valueOf(it.toUpperCase()) } }

    void loaders(List<String> loaders) { this.loaders = loaders.collect { Loader.valueOf(it.toUpperCase()) } }

    void loaders(Loader... loaders) { this.loaders = loaders }

    static Loader[] allLoaders() { return Loader.values() }

    static Loader fabric() { return Loader.FABRIC }

    static Loader quilt() { return Loader.QUILT }

    static Loader forge() { return Loader.FORGE }

    static Loader neoforge() { return Loader.NEOFORGE }

    void minecraft(Object object) {
        if (object instanceof String) {
            this.minecraft = version(object)
        } else if (object instanceof Minecraft) {
            this.minecraft = object
        } else {
            throw new IllegalArgumentException("'minecraft' must be a String or a Minecraft object")
        }
    }

    static Minecraft version(String version) {
        return VersionResolver.getVersionInfoById(version)
    }

    static Minecraft versionCode(String version) {
        return VersionResolver.getVersionInfoById(version)
    }

}
