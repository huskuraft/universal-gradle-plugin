package dev.huskuraft.gradle.plugins.universal

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.huskuraft.gradle.plugins.universal.task.modification.AnnotationModification
import dev.huskuraft.gradle.plugins.universal.task.JarModificationTask
import dev.huskuraft.gradle.plugins.universal.transformer.FabricModJsonTransformer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.tasks.Jar

class UniversalPlugin implements Plugin<Project> {

    static String API_GROUP = "dev.huskuraft.universal"

    static String SHADOW_JAR_TASK = "shadowJar"
    static String TRANSFORM_JAR_TASK = "transformJar"

    static String SHADOW_JAR_TARGET_TASK = "shadowJarTarget"
    static String TRANSFORM_JAR_TARGET_TASK = "transformJarTarget"

    void apply(Project project) {
        project.pluginManager.apply(JavaLibraryPlugin.class)
        project.pluginManager.apply(ShadowPlugin.class)
        project.repositories.add(project.repositories.mavenLocal())
        project.repositories.add(project.repositories.mavenCentral())

        checkProperties(project)

        project.group = project.properties.mod_group_id
        project.version = project.properties.mod_version

        def extension = project.extensions.create('universal', UniversalExtension.class)

        project.dependencies {
            implementation 'com.google.code.findbugs:jsr305:3.0.2'

            annotationProcessor 'com.google.auto.service:auto-service:1.1.1'
            compileOnly 'com.google.auto.service:auto-service:1.1.1'

            compileOnly 'io.netty:netty-all:4.1.109.Final'
            compileOnly 'dev.huskuraft.universal:common-api:+'

            compileOnly 'org.slf4j:slf4j-api:2.0.13'
        }

        project.afterEvaluate {
            registerTasks(project)
            extension.includeTargets.get().forEach {
                setupTarget(project, it)
            }
        }

    }

    private static void checkProperties(Project project) {
        [
            'mod_id',
            'mod_name',
            'mod_license',
            'mod_version',
            'mod_group_id',
            'mod_authors',
            'mod_description',
            'mod_display_url',
            'mod_sources_url',
            'mod_issues_url',
        ].forEach {
            if (project.properties.get(it) == null) {
                throw new IllegalStateException("'${it}' not found in properties")
            }
        }
    }

    private static void setupMod(Project project, UniversalMod mod) {
    }

    private static void registerTasks(Project project) {
        def minecraftShadowJar = SHADOW_JAR_TARGET_TASK
        def shadowJar = SHADOW_JAR_TASK

        project.tasks.register(minecraftShadowJar, ShadowJar.class, task -> {
            task.setGroup("shadow")
        })

        project.tasks.named(shadowJar, ShadowJar.class, task -> {
            task.dependsOn('jar')
            task.archiveClassifier.set("")
            task.mergeServiceFiles()
            task.dependencyFilter.exclude(task.dependencyFilter.dependency("com.google.code.findbugs:jsr305"))

            task.relocate(API_GROUP, project.group.toString())
//            task.relocate(NIGHT_CONFIG_GROUP, "${project.group.toString()}.api.nightconfig")
        })

        project.tasks.named("build", build -> {
            build.dependsOn(minecraftShadowJar)
        })
    }

    private static void setupTarget(Project project, UniversalTarget target) {
        def shadowJarTarget = SHADOW_JAR_TARGET_TASK
        def transformJarTarget = TRANSFORM_JAR_TARGET_TASK

        def versionId = target.minecraft.id
        def versionCode = target.minecraft.dataVersion

        def configuration = "minecraft" + versionCode + "CompileOnly"
        def apiDep = "dev.huskuraft.universal:common-api"
        def targetDeps = target.loaders.collect {
            "dev.huskuraft.universal:${it.name().toLowerCase()}-api-v${versionCode}" as String
        }

        def shadowJarTargetVersionCode = shadowJarTarget + versionCode
        def transformJarTargetVersionCode = transformJarTarget + versionCode

        println("Preparing universal target: ${versionId} (${versionCode}), loaders: ${target.loaders.join(", ")}")

        project.configurations.create(configuration, { Configuration conf ->
            conf.canBeResolved = true
            conf.canBeConsumed = false
            conf.extendsFrom(project.configurations.named('compileOnly').get())
        })

        project.dependencies.add(configuration, "${apiDep}:+")
        for (def targetDep in targetDeps) {
            project.dependencies.add(configuration, "${targetDep}:+")
        }

        def shadowJarTargetTask = project.tasks.register(shadowJarTargetVersionCode, ShadowJar.class, task -> {
            task.group = "shadow"
            task.archiveClassifier.set(versionId)
            task.from(project.extensions.getByType(JavaPluginExtension).sourceSets.main.output)
            task.configurations = [project.configurations.named(configuration).get()]

            task.mergeServiceFiles()
            task.dependencyFilter.include(task.dependencyFilter.dependency(apiDep))
            for (def targetDep in targetDeps) {
                task.dependencyFilter.include(task.dependencyFilter.dependency(targetDep))
            }
            task.transform(new FabricModJsonTransformer())

            task.relocate(API_GROUP, project.group.toString())

        })

        def transformJarTargetTask = project.tasks.register(transformJarTargetVersionCode, JarModificationTask.class, task -> {
            task.group = 'transform'
            task.inputFile = shadowJarTargetTask.get().archiveFile

            task.modification(new AnnotationModification(
                descriptor: "Lnet/minecraftforge/fml/common/Mod;",
                field: "modid",
                newValue: "${project.properties.mod_id}"
            ))
        })

        transformJarTargetTask.get().dependsOn(shadowJarTargetTask.get())
        shadowJarTargetTask.get().finalizedBy(transformJarTargetTask.get())

        project.tasks.named(shadowJarTarget, ShadowJar.class, shadow -> shadow.dependsOn(shadowJarTargetVersionCode))
        project.tasks.named("jar", Jar.class, jar -> jar.setEnabled(false))
    }

}
