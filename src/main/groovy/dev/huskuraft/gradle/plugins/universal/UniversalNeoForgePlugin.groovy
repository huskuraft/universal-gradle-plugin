package dev.huskuraft.gradle.plugins.universal


import org.gradle.api.Plugin
import org.gradle.api.Project

class UniversalNeoForgePlugin implements Plugin<Project> {
    void apply(Project project) {

        project.pluginManager.apply('net.neoforged.gradle.userdev')

        project.dependencies {
            compileOnly "net.neoforged:neoforge:${project.neo_version}"
            compileOnly('net.sf.jopt-simple:jopt-simple:5.0.4') { version { strictly '5.0.4' } }
        }

        project.accessTransformers {
            file('src/main/resources/META-INF/neoforge.accesstransformer.cfg')
        }
        project.runs {
            configureEach {
                workingDirectory project.file('run')
                systemProperty 'forge.logging.markers', 'REGISTRIES'
                systemProperty 'forge.logging.console.level', 'debug'
            }

            client {
                systemProperty 'forge.enabledGameTestNamespaces', project.mod_id
            }

            server {
                systemProperty 'forge.enabledGameTestNamespaces', project.mod_id
                programArgument '--nogui'
            }

            gameTestServer {
                systemProperty 'forge.enabledGameTestNamespaces', project.mod_id
            }
        }

//        project.sourceSets.main.java {
//            srcDir "${project(":common-api").projectDir}/src/main/java"
//        }

    }

}
