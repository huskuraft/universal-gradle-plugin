package dev.huskuraft.gradle.plugins.universal

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.huskuraft.gradle.plugins.universal.plugin.RenameForgeModIdPlugin
import dev.huskuraft.gradle.plugins.universal.transformer.FabricModJsonTransformer
import net.bytebuddy.build.gradle.ByteBuddyPlugin
import net.bytebuddy.build.gradle.ByteBuddyTaskExtension
import net.bytebuddy.build.gradle.PluginArgument
import net.bytebuddy.build.gradle.Transformation
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.tasks.Jar

class UniversalPlugin implements Plugin<Project> {

    static String API_GROUP = "dev.huskuraft.universal"
//    static String NIGHT_CONFIG_GROUP = "com.electronwill.nightconfig"

    static String SHADOW_JAR_TASK = "shadowJar"
    static String FUSE_JAR_TASK = "fuseJar"

    static String SHADOW_JAR_TARGET_TASK = "shadowJarTarget"
    static String FUSE_JAR_TARGET_TASK = "fuseJarTarget"

    void apply(Project project) {
        project.pluginManager.apply(ShadowPlugin.class)
        project.pluginManager.apply(ByteBuddyPlugin.class)

        def extension = project.extensions.create('universal', UniversalExtension.class)

        project.dependencies {
            implementation 'com.google.code.findbugs:jsr305:3.0.2'

            annotationProcessor 'com.google.auto.service:auto-service:1.1.1'
            compileOnly 'com.google.auto.service:auto-service:1.1.1'

            compileOnly 'io.netty:netty-all:4.1.109.Final'
            compileOnly 'dev.huskuraft.universal:common-api:+'
            byteBuddy 'dev.huskuraft.universal:common-api:+'
        }

        setupAsm(project)

        project.afterEvaluate {
            registerTasks(project)
            extension.includeTargets.get().forEach {
                setupTarget(project, it)
            }
        }

    }

    private static void checkExtension(UniversalExtension extension) {
        if (!extension.id.present) {
            throw new IllegalStateException("Mod id is not set in universal {}")
        }
        if (!extension.name.present) {
            throw new IllegalStateException("Mod name is not set in universal {}")
        }
        if (!extension.license.present) {
            throw new IllegalStateException("Mod license is not set in universal {}")
        }
    }

    private static void setupMod(Project project, UniversalMod mod) {
    }

    private static void registerTasks(Project project) {
        def minecraftShadowJar = SHADOW_JAR_TARGET_TASK
//        def minecraftFuseJar = FUSE_JAR_TARGET_TASK
        def shadowJar = SHADOW_JAR_TASK
//        def fuseJar = FUSE_JAR_TASK

        project.tasks.register(minecraftShadowJar, ShadowJar.class, task -> {
            task.setGroup("shadow")
        })

//        project.tasks.register(minecraftFuseJar, FuseJar.class, task -> {
//            task.setGroup("fuse")
//        })

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
        def fuseJarTarget = FUSE_JAR_TARGET_TASK
//        def shadowJar = SHADOW_JAR_TASK
//        def fuseJar = FUSE_JAR_TASK

        def versionId = target.minecraft.id
        def versionCode = target.minecraft.dataVersion

        def configuration = "minecraft" + versionCode + "CompileOnly"
        def apiDep = "dev.huskuraft.universal:common-api"
        def targetDeps = target.loaders.collect {
            "dev.huskuraft.universal:${it.name().toLowerCase()}-api-v${versionCode}" as String
        }

        def shadowJarTargetVersionCode = shadowJarTarget + versionCode
//        def fuseJarTargetVersionCode = fuseJarTarget + versionCode

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

        project.tasks.register(shadowJarTargetVersionCode, ShadowJar.class, task -> {
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

//        project.tasks.register(fuseJarTargetVersionCode, FuseJar.class, task -> {
//            task.group = "fuse"
//            task.archiveClassifier.set(versionId)
//            task.dependsOn(shadowJar)
//            task.dependsOn(shadowJarTargetVersionCode)
//
//            task.mergeServiceFiles()
//            task.includeJar(project.tasks.named(shadowJar, ShadowJar.class).get().archiveFile)
//            task.includeJar(project.tasks.named(shadowJarTargetVersionCode, ShadowJar.class).get().archiveFile)
//        })

        project.tasks.named(shadowJarTarget, ShadowJar.class, shadow -> shadow.dependsOn(shadowJarTargetVersionCode))
//        project.tasks.named(fuseJarTarget, FuseJar.class, fuse -> fuse.dependsOn(fuseJarTargetVersionCode))
        project.tasks.named("jar", Jar.class, jar -> jar.setEnabled(false))
    }

    private static void setupAsm(Project project) {
        UniversalTarget.primaryTargets().toList().forEach {
            setupAsmTarget(project, null, it)
        }

        project.extensions.getByType(ByteBuddyTaskExtension.class).transformation { Transformation transformation ->
            transformation.plugin = RenameForgeModIdPlugin
            transformation.argument { PluginArgument argument ->
                argument.value = project.properties.mod_id
            }
        }
    }

    private static void setupAsmTarget(Project project, UniversalMod mod, UniversalTarget target) {
        def versionCode = target.minecraft.dataVersion

        def targetDeps = target.loaders.collect {
            "dev.huskuraft.universal:${it.name().toLowerCase()}-api-v${versionCode}" as String
        }
        for (def targetDep in targetDeps) {
            project.dependencies.add("byteBuddy", "${targetDep}:+")
        }
    }


}
