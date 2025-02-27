package dev.huskuraft.universal.gradle

import groovy.transform.Immutable
import org.gradle.api.Project

@Immutable
class Mod {
    String id
    String name
    String license
    String version
    Environment environment
    String groupId
    List<String> authors
    String description
    String displayUrl
    String sourcesUrl
    String issuesUrl

    String curseforgeId
    String modrinthId

}
