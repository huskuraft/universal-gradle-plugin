package dev.huskuraft.gradle.plugins.universal.task.modification

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dev.huskuraft.gradle.plugins.universal.Environment
import dev.huskuraft.gradle.plugins.universal.Mod

import java.util.zip.ZipEntry

class FabricModJsonModification extends JsonModification {

    static final String FILE_NAME = 'fabric.mod.json'

    private final Mod mod

    FabricModJsonModification(Mod mod) {
        this.mod = mod
    }

    @Override
    boolean appliesTo(ZipEntry entry) {
        return entry.name.endsWith(FILE_NAME)
    }

    @Override
    protected void modifyJson(JsonObject jsonObject) {
        jsonObject.addProperty("id", mod.id)
        jsonObject.addProperty("name", mod.name)
        jsonObject.addProperty("version", mod.version)
        jsonObject.addProperty("description", mod.description)

        def authorsArray = new JsonArray()
        mod.authors.forEach { authorsArray.add(it) }
        jsonObject.add("authors", authorsArray)

        def contactObject = jsonObject.get("contact").asJsonObject
        contactObject.addProperty("homepage", mod.displayUrl)
        contactObject.addProperty("issues", mod.issuesUrl)
        contactObject.addProperty("sources", mod.sourcesUrl)

        jsonObject.add("contact", contactObject)
        jsonObject.addProperty("license", mod.license)

        switch (mod.environment) {
            case Environment.BOTH:
                jsonObject.addProperty("environment", "*")
                break
            case Environment.CLIENT:
                jsonObject.addProperty("environment", "client")
                break
            case Environment.SERVER:
                jsonObject.addProperty("environment", "server")
                break
        }

        def entrypointsObject = jsonObject.get("entrypoints").asJsonObject

        def mainEntrypointArray = new JsonArray()
        mainEntrypointArray.add("${mod.groupId}.fabric.platform.FabricInitializer")
        entrypointsObject.add("main", mainEntrypointArray)

        def clientEntrypointArray = new JsonArray()
        clientEntrypointArray.add("${mod.groupId}.fabric.platform.FabricClientInitializer")
        entrypointsObject.add("client", clientEntrypointArray)

        def mixinsArray = new JsonArray()
        mixinsArray.add("${mod.id}.mixins.json")
        jsonObject.add("mixins", mixinsArray)

        jsonObject.addProperty("accessWidener", "${mod.id}.accesswidener")
    }
}
