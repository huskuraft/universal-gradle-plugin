package dev.huskuraft.gradle.plugins.universal

import dev.huskuraft.gradle.plugins.universal.versioning.VersionResolver
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.publish.maven.MavenPublication

class UniversalLibraryPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.pluginManager.apply('java-library')
        project.pluginManager.apply('maven-publish')
        project.pluginManager.apply('com.gradleup.shadow')
        project.group = "dev.huskuraft.universal"

        def minecraftVersion = VersionResolver.getVersionInfoById("${project.minecraft_version}")

        project.ext {
            minecraft_version_earliest = "${project.minecraft_version_list}".split(",")[0]
            if (project.loader_name == 'fabric' || project.loader_name == 'quilt') {
                minecraft_version_range = ">=${project.minecraft_version_earliest}-"
            }
            if (project.loader_name == 'forge' || project.loader_name == 'neoforge') {
                minecraft_version_range = "[${project.minecraft_version_earliest},)"
            }
            forge_version_range = "[0,)"
            neoforge_version_range = "[0,)"
            loader_version_range = "[0,)"
            mapping_channel = "official"
            mapping_version = project.minecraft_version

            mod_id='universal'
            mod_version=project.version
            mod_authors='Huskuraft'
            mod_name='Universal API'
            mod_license='LGPLv3'
            mod_description=''
            mod_display_url='https://github.com/huskuraft/universal-api'
            mod_sources_url='https://github.com/huskuraft/universal-api'
            mod_issues_url='https://github.com/huskuraft/universal-api/issues'

        }

        project.dependencies {
            compileOnly project.project(':common-api')
            compileOnly project.project(':third-party:open-pac-api-v0')
            if (minecraftVersion.dataVersion < 3442) {
                compileOnly project.project(':third-party:ftb-chunks-api-v0')
            } else {
                compileOnly project.project(':third-party:ftb-chunks-api-v1')
            }
            compileOnly 'com.google.code.findbugs:jsr305:3.0.2'

            annotationProcessor 'com.google.auto.service:auto-service:1.1.1'
            compileOnly 'com.google.auto.service:auto-service:1.1.1'
        }

        project.base {
            archivesName = "${project.loader_name}-api-${minecraftVersion.id}"
        }

        project.java {
            withSourcesJar()
            sourceCompatibility = minecraftVersion.javaVersion
            targetCompatibility = minecraftVersion.javaVersion
        }

        project.processResources {

            duplicatesStrategy = DuplicatesStrategy.INCLUDE

            filesMatching(['fabric.mod.json', 'META-INF/mods.toml', 'META-INF/neoforge.mods.toml', 'pack.mcmeta']) {
                expand project.properties
            }
        }

        project.jar {
            manifest {
                attributes([
                    'Specification-Title'     : project.mod_id,
                    'Specification-Vendor'    : project.mod_authors,
                    'Specification-Version'   : '1',
                    'Implementation-Title'    : project.name,
                    'Implementation-Version'  : project.jar.archiveVersion,
                    'Implementation-Vendor'   : '',
                    'Implementation-Timestamp': new Date().format("yyyy-MM-dd'T'HH:mm:ssZ")
                ])
            }
        }

        project.shadowJar {
            configurations = [project.configurations.shadow]
            mergeServiceFiles()
            dependencies {
                exclude dependency('com.google.code.findbugs:jsr305')
            }
            relocate 'dev.huskuraft.universal.vanilla', "dev.huskuraft.universal.${project.loader_name}"

            archiveClassifier.set('')
        }

        project.artifacts {
            archives(project.tasks.shadowJar)
        }

        project.publishing {
            publications {
                maven(MavenPublication) {
                    artifactId = "${project.loader_name}-api-v${minecraftVersion.dataVersion}"
                    artifact project.shadowJar
                }
            }
        }

        switch (project.loader_name) {
            case 'fabric':
                project.pluginManager.apply(UniversalFabricPlugin.class)
                break
            case 'quilt':
                project.pluginManager.apply(UniversalQuiltPlugin.class)
                break
            case 'forge':
                project.pluginManager.apply(UniversalForgePlugin.class)
                break
            case 'neoforge':
                project.pluginManager.apply(UniversalNeoForgePlugin.class)
                break
        }


    }

}
