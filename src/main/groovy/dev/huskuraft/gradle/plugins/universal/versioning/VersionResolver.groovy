package dev.huskuraft.gradle.plugins.universal.versioning

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.gradle.api.JavaVersion

class VersionResolver {

    private static final Map<String, Minecraft> versionMap
    private static final Gson gson = new Gson()

    static {
        versionMap = loadVersions()
    }

    private static Map<String, Minecraft> loadVersions() {
        String jsonString = VersionResolver.class.getClassLoader().getResource('data.json').getText()

        def listType = new TypeToken<List<Minecraft>>() {}.getType()
        def versionList = gson.fromJson(jsonString, listType)
        return versionList.collectEntries { [(it.id): it] }
    }

    static Minecraft getVersionInfoById(String id) {
        def versionInfo = versionMap[id]
        if (versionInfo) {
            return versionInfo
        } else {
            throw new IllegalArgumentException("Version with id $id not found.")
        }
    }

    static JavaVersion getJavaVersion(int dataVersion) {
        switch (dataVersion) {
            case 0000..2503: return JavaVersion.VERSION_1_8
            case 2504..2680: return JavaVersion.VERSION_11
            case 2681..2833: return JavaVersion.VERSION_17 // JavaVersion.VERSION_16
            case 2834..3800: return JavaVersion.VERSION_17
            case 3801..4315: return JavaVersion.VERSION_21
            default: return JavaVersion.VERSION_21
        }
    }

}
