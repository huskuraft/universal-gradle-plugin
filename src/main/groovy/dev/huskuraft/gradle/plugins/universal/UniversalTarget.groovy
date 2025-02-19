package dev.huskuraft.gradle.plugins.universal

import dev.huskuraft.gradle.plugins.universal.versioning.Minecraft
import dev.huskuraft.gradle.plugins.universal.versioning.VersionResolver

class UniversalTarget {

    Minecraft minecraft
    Loader[] loaders

    void loaders(String... loaders) { this.loaders = loaders.collect { Loader.valueOf(it) } }

    void loaders(Loader... loaders) { this.loaders = loaders }

    static Loader[] allLoaders() { return Loader.values() }

    static Loader fabric() { return Loader.FABRIC }

    static Loader quilt() { return Loader.QUILT }

    static Loader forge() { return Loader.FORGE }

    static Loader neoforge() { return Loader.NEOFORGE }

    void minecraft(Minecraft minecraft) {
        this.minecraft = minecraft
    }

    static Minecraft version(String version) {
        return VersionResolver.getVersionInfoById(version)
    }

    static enum Loader {
        FABRIC, QUILT, FORGE, NEOFORGE
    }

    static UniversalTarget[] primaryTargets() {
        return [
            new UniversalTarget(minecraft: version('1.17.1'), loaders: [fabric(), quilt(), forge()]),
            new UniversalTarget(minecraft: version('1.18.1'), loaders: [fabric(), quilt(), forge()]),
            new UniversalTarget(minecraft: version('1.18.2'), loaders: [fabric(), quilt(), forge()]),
            new UniversalTarget(minecraft: version('1.19.2'), loaders: [fabric(), quilt(), forge()]),
            new UniversalTarget(minecraft: version('1.19.3'), loaders: [fabric(), quilt(), forge()]),
            new UniversalTarget(minecraft: version('1.19.4'), loaders: [fabric(), quilt(), forge()]),
            new UniversalTarget(minecraft: version('1.20.1'), loaders: [fabric(), quilt(), forge()]),
            new UniversalTarget(minecraft: version('1.20.2'), loaders: [fabric(), quilt(), forge()]),
            new UniversalTarget(minecraft: version('1.20.4'), loaders: [fabric(), quilt(), forge()]),
            new UniversalTarget(minecraft: version('1.20.6'), loaders: [fabric(), quilt(), forge(), neoforge()]),
            new UniversalTarget(minecraft: version('1.21.1'), loaders: [fabric(), quilt(), forge(), neoforge()]),
            new UniversalTarget(minecraft: version('1.21.3'), loaders: [fabric(), quilt(), forge(), neoforge()]),
        ]
    }

    static UniversalTarget[] allTargets() {
        return [
            new UniversalTarget(minecraft: version('1.17.1'), loaders: [fabric(), quilt(), forge()]),
            new UniversalTarget(minecraft: version('1.18'),   loaders: [fabric(), quilt(), forge()]),
            new UniversalTarget(minecraft: version('1.18.1'), loaders: [fabric(), quilt(), forge()]),
            new UniversalTarget(minecraft: version('1.18.2'), loaders: [fabric(), quilt(), forge()]),
            new UniversalTarget(minecraft: version('1.19'),   loaders: [fabric(), quilt(), forge()]),
            new UniversalTarget(minecraft: version('1.19.1'), loaders: [fabric(), quilt(), forge()]),
            new UniversalTarget(minecraft: version('1.19.2'), loaders: [fabric(), quilt(), forge()]),
            new UniversalTarget(minecraft: version('1.19.3'), loaders: [fabric(), quilt(), forge()]),
            new UniversalTarget(minecraft: version('1.19.4'), loaders: [fabric(), quilt(), forge()]),
            new UniversalTarget(minecraft: version('1.20'),   loaders: [fabric(), quilt(), forge()]),
            new UniversalTarget(minecraft: version('1.20.1'), loaders: [fabric(), quilt(), forge()]),
            new UniversalTarget(minecraft: version('1.20.2'), loaders: [fabric(), quilt(), forge()]),
            new UniversalTarget(minecraft: version('1.20.3'), loaders: [fabric(), quilt(), forge()]),
            new UniversalTarget(minecraft: version('1.20.4'), loaders: [fabric(), quilt(), forge()]),
            new UniversalTarget(minecraft: version('1.20.5'), loaders: [fabric(), quilt(), forge()]),
            new UniversalTarget(minecraft: version('1.20.6'), loaders: [fabric(), quilt(), forge(), neoforge()]),
            new UniversalTarget(minecraft: version('1.21'),   loaders: [fabric(), quilt(), forge(), neoforge()]),
            new UniversalTarget(minecraft: version('1.21.1'), loaders: [fabric(), quilt(), forge(), neoforge()]),
            new UniversalTarget(minecraft: version('1.21.2'), loaders: [fabric(), quilt(), forge(), neoforge()]),
            new UniversalTarget(minecraft: version('1.21.3'), loaders: [fabric(), quilt(), forge(), neoforge()]),
            new UniversalTarget(minecraft: version('1.21.4'), loaders: [fabric(), quilt(), forge(), neoforge()]),
        ]
    }




}
