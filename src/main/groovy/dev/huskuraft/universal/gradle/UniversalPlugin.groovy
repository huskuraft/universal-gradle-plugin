package dev.huskuraft.universal.gradle

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.huskuraft.universal.gradle.task.JarModificationTask
import dev.huskuraft.universal.gradle.task.modification.fabric.FabricAccessWidenerRenameModification
import dev.huskuraft.universal.gradle.task.modification.fabric.FabricMixinsJsonPropertyModification
import dev.huskuraft.universal.gradle.task.modification.fabric.FabricModJsonPropertyModification
import dev.huskuraft.universal.gradle.task.modification.fabric.FabricMixinsJsonRenameModification
import dev.huskuraft.universal.gradle.task.modification.fabric.FabricRefmapJsonPropertyModification
import dev.huskuraft.universal.gradle.task.modification.fabric.FabricRefmapJsonRenameModification
import dev.huskuraft.universal.gradle.task.modification.forge.ForgeAnnotationModification
import dev.huskuraft.universal.gradle.task.modification.forge.ForgeModTomlModification
import dev.huskuraft.universal.gradle.task.modification.neoforge.NeoForgeAnnotationModification
import dev.huskuraft.universal.gradle.task.modification.neoforge.NeoForgeModTomlModification
import dev.huskuraft.universal.gradle.versioning.Minecraft
import dev.huskuraft.universal.gradle.versioning.VersionResolver
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPluginExtension

class UniversalPlugin implements Plugin<Project> {

    static String API_GROUP = "dev.huskuraft.universal"

    static String SHADOW_JAR_TASK = "shadowJar"
    static String TRANSFORM_JAR_TASK = "transformJar"

    static String SHADOW_JAR_TARGET_TASK = "shadowJarTarget"
    static String TRANSFORM_JAR_TARGET_TASK = "transformJarTarget"

    void apply(Project project) {
        project.pluginManager.apply(JavaLibraryPlugin.class)
        project.pluginManager.apply(ShadowPlugin.class)

        checkProperties(project)

        project.group = project.properties.mod_group_id
        project.version = project.properties.mod_version

        def extension = project.extensions.create('universal', UniversalExtension.class)


        project.configurations.create('universalTarget', { Configuration conf ->
            conf.canBeResolved = true
            conf.canBeConsumed = false
        })


        project.afterEvaluate {
            registerTasks(project)
            setupTargets(project)
        }

    }

    private static Map<String, Object> apis = [
        'fabric-api': Loader.FABRIC,
        'quilt-api': Loader.QUILT,
        'forge-api': Loader.FORGE,
        'neoforge-api': Loader.NEOFORGE
    ]

    private static void setupTargets(Project project) {
        def commonApiSet = project.configurations.named('implementation').get().dependencies.findAll {
            it.group == 'dev.huskuraft.universal' && it.name == 'common-api'
        }
        if (commonApiSet.isEmpty()) {
            throw new IllegalStateException("common-api not found in implementation")
        }
        if (commonApiSet.size() > 1) {
            throw new IllegalStateException("common-api found multiple times in implementation")
        }

        def commonApi = commonApiSet.iterator().next()

        def universalTargets = project.configurations.named('universalTarget').get().dependencies.toList()

        if (universalTargets.isEmpty()) {
            throw new IllegalStateException("No universal targets found in universalTarget")
        }

        universalTargets.forEach { targetApi ->
            def minecraftVersion = VersionResolver.findById(targetApi.version)
            if (targetApi.group != 'dev.huskuraft.universal' || !apis[targetApi.name] || minecraftVersion.isEmpty()) {
                throw new IllegalStateException("Invalid universal target: ${targetApi.group}:${targetApi.name}:${targetApi.version}")
            }

            project.logger.info("Preparing universal target: ${targetApi.group}:${targetApi.name}:${targetApi.version}")

            setupTarget(project, minecraftVersion.get(), targetApi.name, commonApi.version)

        }
    }



    private static void checkProperties(Project project) {
        [
            'mod_id',
            'mod_name',
            'mod_license',
            'mod_version',
            'mod_environment',
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
        [
            'mod_modrinth_id',
        ].forEach {
            if (project.properties.get(it) == null) {
                project.logger.warn("'${it}' not found in properties, skipping modrinth upload")
            }
        }
        [
            'mod_curseforge_id',
        ].forEach {
            if (project.properties.get(it) == null) {
                project.logger.warn("'${it}' not found in properties, skipping curseforge upload")
            }
        }
    }

    private static void registerTasks(Project project) {
        def shadowJarTarget = SHADOW_JAR_TARGET_TASK
        def shadowJar = SHADOW_JAR_TASK
        def transformJarTarget = TRANSFORM_JAR_TARGET_TASK
        def transformJar = TRANSFORM_JAR_TASK

        project.tasks.register(transformJarTarget, task -> {
            task.dependsOn(shadowJarTarget)
            task.group = 'transform'
        })

        project.tasks.register(shadowJarTarget, ShadowJar.class, task -> {
            task.group = 'shadow'
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
            build.dependsOn(transformJarTarget)
        })
    }

    private static void setupTarget(Project project, Minecraft minecraft, String api, String apiVersion) {
        def shadowJarTarget = SHADOW_JAR_TARGET_TASK
        def transformJarTarget = TRANSFORM_JAR_TARGET_TASK

        def minecraftId = minecraft.id
        def dataVersion = minecraft.dataVersion

        def configuration = "minecraft" + dataVersion + "CompileOnly"
        def apiDep = "dev.huskuraft.universal:common-api:${apiVersion}"
        def targetDep = "dev.huskuraft.universal:${api}:${apiVersion}:v${dataVersion}" as String

        def shadowJarTargetVersionCode = shadowJarTarget + dataVersion
        def transformJarTargetVersionCode = transformJarTarget + dataVersion

        def conf = project.configurations.maybeCreate(configuration)
        conf.canBeResolved = true
        conf.canBeConsumed = false
        conf.extendsFrom(project.configurations.named('compileOnly').get())

        project.dependencies.add(configuration, "${apiDep}")
        project.dependencies.add(configuration, "${targetDep}")

        def shadowJarTargetTaskNotCreated = project.tasks.findByName(shadowJarTargetVersionCode) != null
        def shadowJarTargetTask = project.tasks.maybeCreate(shadowJarTargetVersionCode, ShadowJar.class)
        if (shadowJarTargetTaskNotCreated) {
            shadowJarTargetTask.group = "shadow"
            shadowJarTargetTask.archiveAppendix.set(minecraftId)
            shadowJarTargetTask.from(project.extensions.getByType(JavaPluginExtension).sourceSets.main.output)
            shadowJarTargetTask.configurations = [project.configurations.named(configuration).get()]

            shadowJarTargetTask.mergeServiceFiles()
            shadowJarTargetTask.relocate(API_GROUP, project.group.toString())
        }
        shadowJarTargetTask.dependencyFilter.include(shadowJarTargetTask.dependencyFilter.dependency(apiDep))
        shadowJarTargetTask.dependencyFilter.include(shadowJarTargetTask.dependencyFilter.dependency(targetDep))

        def transformJarTargetTaskNotCreated = project.tasks.findByName(transformJarTargetVersionCode) != null
        def transformJarTargetTask = project.tasks.maybeCreate(transformJarTargetVersionCode, JarModificationTask.class)
        if (transformJarTargetTaskNotCreated) {
            transformJarTargetTask.dependsOn(shadowJarTargetVersionCode)
            transformJarTargetTask.group = 'transform'
            transformJarTargetTask.inputFile = shadowJarTargetTask.archiveFile
            transformJarTargetTask.outputFile = shadowJarTargetTask.archiveFile
        }
        def mod = createMod(project)
        switch (apis[api]) {
            case Loader.FABRIC:
                transformJarTargetTask.modification(new FabricModJsonPropertyModification(mod))

                transformJarTargetTask.modification(new FabricMixinsJsonPropertyModification(mod))
                transformJarTargetTask.modification(new FabricMixinsJsonRenameModification(mod))

                transformJarTargetTask.modification(new FabricRefmapJsonPropertyModification(mod))
                transformJarTargetTask.modification(new FabricRefmapJsonRenameModification(mod))

                transformJarTargetTask.modification(new FabricAccessWidenerRenameModification(mod))

                break
            case Loader.QUILT:
                break
            case Loader.FORGE:

                transformJarTargetTask.modification(new ForgeModTomlModification(mod))
                transformJarTargetTask.modification(new ForgeAnnotationModification(mod))
                break
            case Loader.NEOFORGE:

                transformJarTargetTask.modification(new NeoForgeModTomlModification(mod))
                transformJarTargetTask.modification(new NeoForgeAnnotationModification(mod))
                break

        }

        project.tasks.named(shadowJarTarget,task -> task.dependsOn(shadowJarTargetVersionCode))
        project.tasks.named(transformJarTarget, task -> task.dependsOn(transformJarTargetVersionCode))

        project.tasks.named("jar", task -> task.setEnabled(false))
    }


    static Mod createMod(Project project) {
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
            project.properties.mod_curseforge_id.toString(),
            project.properties.mod_modrinth_id.toString(),
        )
    }

}
