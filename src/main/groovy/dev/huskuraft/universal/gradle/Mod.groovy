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

    static Mod create(Project project) {
        return new Mod(
            project.properties.mod_id.toString(),
            project.properties.mod_name.toString(),
            project.properties.mod_license.toString(),
            project.properties.mod_version.toString(),
            Environment.valueOf(project.properties.mod_environment.toString().toUpperCase()),
            project.properties.mod_group_id.toString(),
            project.properties.mod_authors.toString().split(",").toList(),
            project.properties.mod_description.toString(),
            project.properties.mod_display_url.toString(),
            project.properties.mod_sources_url.toString(),
            project.properties.mod_issues_url.toString(),
        )
    }
}
