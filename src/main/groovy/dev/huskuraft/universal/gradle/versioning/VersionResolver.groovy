package dev.huskuraft.universal.gradle.versioning

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class VersionResolver {

    private static final Map<String, Minecraft> versionMap
    private static final Gson gson = new Gson()

    static {
        versionMap = loadVersions()
    }

    private static Map<String, Minecraft> loadVersions() {
        String jsonString = VersionResolver.class.getClassLoader().getResource('data.json').getText()

        def listType = new TypeToken<List<Minecraft>>() {}.getType()
        def versionList = gson.fromJson(jsonString, listType) as Iterable<Minecraft>
        return versionList.collectEntries { [(it.id): it] }
    }

    static Optional<Minecraft> findById(String id) {
        return Optional.of(versionMap[id])
    }

}
