package dev.huskuraft.universal.gradle

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.huskuraft.minecraft.gradle.publish.ModPublishPlugin
import dev.huskuraft.minecraft.gradle.publish.ModPublishingExtension
import dev.huskuraft.universal.gradle.task.JarModificationTask
import dev.huskuraft.universal.gradle.task.modification.fabric.*
import dev.huskuraft.universal.gradle.task.modification.forge.ForgeAnnotationModification
import dev.huskuraft.universal.gradle.task.modification.forge.ForgeModTomlModification
import dev.huskuraft.universal.gradle.task.modification.neoforge.NeoForgeAnnotationModification
import dev.huskuraft.universal.gradle.task.modification.neoforge.NeoForgeModTomlModification
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPluginExtension

class UniversalPlugin implements Plugin<Project> {

    static String API_GROUP = "dev.huskuraft.universal"

    static String SHADOW_JAR_TASK = "shadowJar"
    static String TRANSFORM_JAR_TASK = "transformJar"

    static String SHADOW_JAR_MINECRAFT_TASK = "shadowJarMinecraft"
    static String TRANSFORM_JAR_MINECRAFT_TASK = "transformJarMinecraft"

    void apply(Project project) {
        project.pluginManager.apply(JavaLibraryPlugin.class)
        project.pluginManager.apply(ShadowPlugin.class)

        project.extensions.create('universal', UniversalExtension.class)

        project.afterEvaluate {
            setupProperties(project)

            registerTasks(project)

            setupTargets(project)
            setupReleases(project)
        }


    }

    private static Map<String, Object> API_MAP = ['fabric-api'  : Loader.FABRIC,
                                                  'quilt-api'   : Loader.QUILT,
                                                  'forge-api'   : Loader.FORGE,
                                                  'neoforge-api': Loader.NEOFORGE]


    private static Map<String, Object> API_NAME_MAP = [
        'fabric-api'  : 'Fabric',
        'quilt-api'   : 'Quilt',
        'forge-api'   : 'Forge',
        'neoforge-api': 'NeoForge'
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

        def extension = project.extensions.getByType(UniversalExtension.class)

        if (extension.targets.get().isEmpty()) {
            throw new IllegalStateException("No universal targets found in universal configuration")
        }

        extension.targets.get().forEach { minecraft, apis ->
            apis.forEach { api ->
                setupTarget(project, getTargetName(minecraft, apis), minecraft.last, api, commonApi.version)
            }
        }
    }


    private static void setupProperties(Project project) {
        def extension = project.extensions.getByType(UniversalExtension.class)

        ['mod.id'         : extension.id,
         'mod.name'       : extension.name,
         'mod.license'    : extension.license,
         'mod.environment': extension.environment,
         'mod.description': extension.description,].forEach { property, value ->
            if (!value.present) value.set(project.properties.get(property) as String)
            if (!value.present) throw new IllegalStateException("'${property}' in properties or '${property.replace("mod.", "")}' in universal configuration not found ")
        }

        if (!Environment.values().collectEntries { [it.name().toLowerCase(), it] }.containsKey(extension.environment.get())) {
            def property = 'mod.environment'
            throw new IllegalStateException("'${property}' in properties or '${property.replace("mod.", "")}' in universal configuration should be one of ${Environment.values().collect { it.name().toLowerCase() }.join(", ")}")
        }

        ['mod.primaryUrl': extension.primaryUrl,
         'mod.sourcesUrl': extension.sourcesUrl,
         'mod.supportUrl': extension.supportUrl,].forEach { property, value ->
            if (!value.present) value.set(new URI(project.properties.get(property) as String))
            if (!value.present) throw new IllegalStateException("'${property}' in properties or '${property.replace("mod.", "")}' in universal configuration not found ")
        }

        ['mod.authors': extension.authors,].forEach { property, value ->
            if (!value.present) value.set((project.properties.get(property) as String).split(",").toList())
            if (!value.present) throw new IllegalStateException("'${property}' in properties or '${property.replace("mod.", "")}' in universal configuration not found ")
        }

        ['mod.modrinth.id'   : extension.modrinth.id,
         'mod.modrinth.token': extension.modrinth.token,].forEach { property, value ->
            if (!value.present) value.set(project.properties.get(property) as String)
            if (!value.present) project.logger.warn("'${property}' in properties or '${property.replace("mod.", "")}' in universal configuration not found, skipping modrinth publication")
        }

        ['mod.curseforge.id'   : extension.curseforge.id,
         'mod.curseforge.token': extension.curseforge.token,].forEach { property, value ->
            if (!value.present) value.set(project.properties.get(property) as String)
            if (!value.present) project.logger.warn("'${property}' in properties or '${property.replace("mod.", "")}' in universal configuration not found, skipping curseforge publication")
        }

        extension.group.set(project.group as String)
        extension.version.set(project.version as String)

    }

    private static void registerTasks(Project project) {
        def shadowJarTarget = SHADOW_JAR_MINECRAFT_TASK
        def shadowJar = SHADOW_JAR_TASK
        def transformJarTarget = TRANSFORM_JAR_MINECRAFT_TASK
        def transformJar = TRANSFORM_JAR_TASK

        project.tasks.register(transformJarTarget, task -> {
            task.dependsOn(shadowJarTarget)
            task.group = 'universal'
        })

        project.tasks.register(shadowJarTarget, ShadowJar.class, task -> {
        })

        project.tasks.named(shadowJar, ShadowJar.class, task -> {
            task.dependsOn('jar')
            task.archiveClassifier.set("")
            task.mergeServiceFiles()
            task.dependencyFilter.exclude(task.dependencyFilter.dependency("com.google.code.findbugs:jsr305"))

            task.relocate(API_GROUP, project.group.toString())
        })

        project.tasks.named("build", build -> {
            build.dependsOn(transformJarTarget)
        })
    }

    private static void setupTarget(Project project, String targetName, String minecraftId, String api, String apiVersion) {
        def shadowJarMinecraft = SHADOW_JAR_MINECRAFT_TASK
        def transformJarMinecraft = TRANSFORM_JAR_MINECRAFT_TASK

        def configuration = targetName.uncapitalize() + "CompileOnly"
        def apiDep = "dev.huskuraft.universal:common-api:${apiVersion}"
        def targetDep = "dev.huskuraft.universal:${api}:${apiVersion}:${minecraftId}" as String

        def shadowJarMinecraftTarget = SHADOW_JAR_TASK + targetName
        def transformJarMinecraftTarget = TRANSFORM_JAR_TASK + targetName

        def conf = project.configurations.maybeCreate(configuration)
        conf.canBeResolved = true
        conf.canBeConsumed = false
        conf.extendsFrom(project.configurations.named('compileOnly').get())

        project.dependencies.add(configuration, "${apiDep}")
        project.dependencies.add(configuration, "${targetDep}")

        def shadowJarTargetTaskNotCreated = project.tasks.findByName(shadowJarMinecraftTarget) != null
        def shadowJarTargetTask = project.tasks.maybeCreate(shadowJarMinecraftTarget, ShadowJar.class)
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

        def transformJarTargetTaskNotCreated = project.tasks.findByName(transformJarMinecraftTarget) != null
        def transformJarTargetTask = project.tasks.maybeCreate(transformJarMinecraftTarget, JarModificationTask.class)
        if (transformJarTargetTaskNotCreated) {
            transformJarTargetTask.dependsOn(shadowJarMinecraftTarget)
            transformJarTargetTask.group = 'universal'
            transformJarTargetTask.inputFile = shadowJarTargetTask.archiveFile
            transformJarTargetTask.outputFile = shadowJarTargetTask.archiveFile
        }

        def extension = project.extensions.getByType(UniversalExtension.class)
        def mod = createMod(extension)
        switch (API_MAP[api]) {
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

        project.tasks.named(shadowJarMinecraft, task -> task.dependsOn(shadowJarMinecraftTarget))
        project.tasks.named(transformJarMinecraft, task -> task.dependsOn(transformJarMinecraftTarget))

        project.tasks.named("jar", task -> task.setEnabled(false))
    }

    private static void setupReleases(Project project) {

        def extension = project.extensions.getByType(UniversalExtension.class)
        def mod = createMod(extension)
        project.pluginManager.apply(ModPublishPlugin.class)
        def publishExtension = project.extensions.getByType(ModPublishingExtension.class)
        publishExtension.publications {
            it.register('release') { publication ->
                publication.modId.set(mod.id)
                publication.modName.set(mod.name)
                publication.changelog {
//                        it.from file('CHANGELOG.md')
                }
                publication.artifacts {
                    extension.targets.get().forEach { minecraft, loaders ->
                        it.register(minecraft.first()) { artifact ->
                            artifact.minecraft.set(minecraft)
                            artifact.loaders.set(loaders)
                            artifact.relations {

                            }
                            def targetName = minecraft.last.replace("-", "").replace(".", "").toUpperCase()
                            def transformJarMinecraftTarget = SHADOW_JAR_TASK + targetName
                            artifact.from(project.tasks.named(transformJarMinecraftTarget, JarModificationTask.class).get().outputFile)
                        }
                    }

                }
            }
        }

        publishExtension.repositories {
            it.curseforge {
                it.authToken.set(extension.curseforge.token)
                it.projectId.set(extension.curseforge.id)
            }
            it.modrinth {
                it.authToken.set(extension.modrinth.token)
                it.projectId.set(extension.modrinth.id)
            }
        }
    }

    static String getTargetName(List<String> minecraft, List<String> apis) {
        return 'Minecraft' + minecraft.last.replace('.', '').replace('-', '').toUpperCase() + apis.collect { API_NAME_MAP[it] }.join('')
    }

    static Mod createMod(UniversalExtension extension) {
        return new Mod(
            extension.id.get(),
            extension.name.get(),
            extension.description.get(),
            extension.authors.get(),
            extension.license.get(),
            Environment.values().collectEntries { [it.name().toLowerCase(), it] }.get(extension.environment.get()) as Environment,
            extension.group.get(),
            extension.version.get(),
            extension.primaryUrl.get(),
            extension.sourcesUrl.get(),
            extension.supportUrl.get(),)
    }


}
