package dev.huskuraft.universal.gradle

import groovy.transform.Immutable
import org.gradle.api.Project

@Immutable
class Mod {
    String id
    String name
    String description
    List<String> authors
    String license
    Environment environment
    String groupId
    String version
    URI primaryUrl
    URI sourcesUrl
    URI supportUrl
}
