# AGENTS.md - Universal Gradle Plugin

## Project Overview

The **Universal Gradle Plugin** is a sophisticated build automation tool designed to simplify the development of multi-platform Minecraft mods. It provides a unified API that abstracts away the differences between various mod loaders (Fabric, Quilt, Forge, NeoForge) and Minecraft versions, allowing developers to write code once and deploy across multiple platforms.

### Purpose

This plugin solves the complex problem of maintaining Minecraft mods across:
- Multiple Minecraft versions (1.17.1 through 1.21.3+)
- Multiple mod loaders (Fabric, Quilt, Forge, NeoForge)
- Different API requirements per platform

Instead of maintaining separate codebases or build configurations for each platform, developers can use this plugin to automate the transformation and packaging of their mod for all target platforms.

## Architecture Overview

### Core Components

```
UniversalPlugin (Main Entry Point)
    ├── UniversalExtension (Configuration DSL)
    ├── Task Management
    │   ├── ShadowJar (JAR packaging)
    │   └── JarModificationTask (Platform-specific transformations)
    ├── Target Setup (Per-platform builds)
    ├── Publishing Integration (Modrinth, CurseForge)
    └── Modification System
        ├── Fabric Modifications
        ├── Forge Modifications
        └── NeoForge Modifications
```

### Key Technologies

- **Groovy**: Primary language for the plugin DSL and configuration
- **Java**: Core task execution and bytecode manipulation
- **Gradle**: Build system integration
- **ASM**: Java bytecode manipulation for annotation modifications
- **Gson**: JSON manipulation for mod metadata
- **Night Config**: TOML file processing for Forge/NeoForge mods
- **Shadow Plugin**: JAR shading and dependency merging

## Core Components Detail

### 1. UniversalPlugin (`UniversalPlugin.groovy`)

**Responsibility**: Main plugin entry point that orchestrates the entire build process.

**Key Functions**:
- Applies necessary Gradle plugins (JavaLibrary, Shadow, ModPublish)
- Creates the `universal {}` DSL extension
- Sets up properties from `gradle.properties` or DSL
- Registers build tasks
- Configures target platforms
- Integrates with publishing platforms

**Critical Flow**:
1. Plugin applied to project
2. `afterEvaluate` hook triggers setup
3. Properties validated and resolved
4. Tasks registered for each target platform
5. Modifications configured per loader type
6. Publishing configured for distribution

### 2. UniversalExtension (`UniversalExtension.groovy`)

**Responsibility**: Provides the DSL configuration interface for users.

**Configuration Properties**:
- **Mod Metadata**: `id`, `name`, `description`, `authors`, `license`
- **Environment**: `both`, `client`, or `server`
- **URLs**: `primaryUrl`, `sourcesUrl`, `supportUrl`
- **Targets**: Map of Minecraft versions to loader APIs
- **Publishing**: Modrinth and CurseForge credentials

**Example Usage**:
```groovy
universal {
    id = 'mymod'
    name = 'My Universal Mod'
    description = 'Works on all platforms!'
    authors = ['Developer']
    license = 'MIT'
    environment = 'both'
    
    targets = [
        ['1.20.1']: ['fabric-api', 'forge-api'],
        ['1.21.1']: ['fabric-api', 'neoforge-api']
    ]
    
    modrinth {
        token = project.findProperty('modrinth.token')
        id = 'AbCdEfGh'
    }
}
```

### 3. Domain Models

#### Mod (`Mod.groovy`)
Immutable data class representing mod metadata:
- Basic info: id, name, version, description
- Authors and licensing
- Environment targeting
- URL links for homepage, sources, support

#### Loader (`Loader.groovy`)
Enum representing supported mod loaders:
- `FABRIC`: Fabric mod loader
- `QUILT`: Quilt mod loader  
- `FORGE`: MinecraftForge
- `NEOFORGE`: NeoForge (Fork of Forge for 1.20.6+)

#### Environment (`Environment.groovy`)
Enum for execution environment:
- `BOTH`: Client and server
- `CLIENT`: Client-only
- `SERVER`: Server-only

#### Minecraft (`Minecraft.groovy`)
Data class for Minecraft version information:
- Version identifiers and metadata
- Protocol versions
- Data pack and resource pack versions
- Build and release timestamps

### 4. JarModificationTask (`JarModificationTask.java`)

**Responsibility**: Core task that applies platform-specific modifications to JAR files.

**How It Works**:
1. Takes an input JAR file from the shadow task
2. Iterates through all JAR entries
3. Applies relevant modifications based on entry name/type
4. Writes modified JAR to output location

**Key Features**:
- **Chainable Modifications**: Multiple modifications can be applied sequentially
- **Entry-Level Granularity**: Can modify both entry metadata and content
- **Type Safety**: Uses the Modification interface for extensibility
- **In-Place Updates**: Efficiently replaces original JAR

**Architecture**:
```java
JarModificationTask
    └── JarModificationAction
        ├── Reads JAR entries
        ├── Filters applicable modifications
        ├── Applies transformations
        └── Writes modified JAR
```

### 5. Modification System

The modification system is the heart of platform adaptation. Each modification implements the `Modification` interface:

```java
interface Modification {
    boolean appliesTo(JarEntry entry);
    JarEntry apply(JarEntry inputEntry);
    byte[] apply(byte[] input);
}
```

#### Modification Types

**A. JSON Modifications** (`JsonModification.java`)
- Base class for JSON file transformations
- Uses Gson for parsing/serialization
- Provides `modifyJson()` hook for subclasses

**Examples**:
- `FabricModJsonPropertyModification`: Updates `fabric.mod.json` metadata
- `FabricMixinsJsonPropertyModification`: Configures mixin files
- `FabricRefmapJsonPropertyModification`: Updates refmap references

**B. TOML Modifications** (`TomlModification.java`)
- Base class for TOML file transformations
- Uses Night Config library
- Used for Forge/NeoForge `mods.toml`

**Examples**:
- `ForgeModTomlModification`: Updates Forge mod metadata
- `NeoForgeModTomlModification`: Updates NeoForge mod metadata

**C. Annotation Modifications** (`AnnotationModification.java`)
- Uses ASM for bytecode manipulation
- Modifies Java annotations in class files
- Changes annotation values without recompiling

**Examples**:
- `ForgeAnnotationModification`: Updates `@Mod` annotation value
- `NeoForgeAnnotationModification`: Updates NeoForge annotations

**D. Rename Modifications** (`RenameModification.java`)
- Changes file paths within the JAR
- Used for relocating resources to match new package structure

**Examples**:
- `FabricMixinsJsonRenameModification`: Renames mixin config files
- `FabricRefmapJsonRenameModification`: Renames refmap files
- `FabricAccessWidenerRenameModification`: Renames access widener files

**E. Plain Text Modifications** (`PlainTextModification.java`)
- Simple string replacement in text files
- Used for basic configuration updates

### 6. Platform-Specific Modifications

#### Fabric Modifications (`task/modification/fabric/`)

**FabricModJsonPropertyModification**:
- Updates `fabric.mod.json` with mod metadata
- Sets entrypoints for main and client initialization
- Configures mixins and access wideners
- Maps environment settings

**FabricMixinsJsonPropertyModification**:
- Updates mixin configuration files
- Sets package and refmap paths

**FabricRefmapJsonPropertyModification**:
- Updates reference map used by mixins
- Ensures correct class mapping references

**Rename Modifications**:
- Relocates mixin, refmap, and access widener files to match mod ID
- Ensures no conflicts with other mods

#### Forge Modifications (`task/modification/forge/`)

**ForgeModTomlModification**:
- Updates `META-INF/mods.toml` with mod metadata
- Configures dependencies
- Sets display information

**ForgeAnnotationModification**:
- Modifies `@Mod` annotation in main mod class
- Updates mod ID at bytecode level
- Ensures annotation matches configuration

#### NeoForge Modifications (`task/modification/neoforge/`)

Similar to Forge but adapted for NeoForge's API:
- `NeoForgeModTomlModification`: TOML metadata updates
- `NeoForgeAnnotationModification`: Annotation modifications

### 7. Version Management

**VersionResolver** (`versioning/VersionResolver.groovy`):
- Loads Minecraft version metadata from `data.json`
- Provides lookup functionality for version information
- Contains comprehensive data about protocol versions, data pack versions, etc.

**Data File** (`resources/data.json`):
- Comprehensive list of all Minecraft versions
- Includes snapshots, pre-releases, and release versions
- Contains version-specific metadata needed for compatibility

### 8. Publishing Integration

The plugin integrates with the **ModPublishPlugin** to automate distribution:

**Supported Platforms**:
- **Modrinth**: Modern Minecraft mod hosting
- **CurseForge**: Traditional mod hosting platform

**Features**:
- Automatic artifact creation per target
- Version channel detection (alpha, beta, release)
- Metadata propagation from universal configuration
- Per-platform JAR uploads with correct loader tags

## Build Process Flow

### 1. Configuration Phase

```
User defines universal {} block
    ↓
Plugin validates properties
    ↓
Properties merged from gradle.properties
    ↓
Targets validated and expanded
```

### 2. Task Registration

For each target (Minecraft version + Loader):

```
Create configuration: <target>CompileOnly
    ↓
Add dependencies: common-api + platform-api
    ↓
Register shadow<Target>ModJar (ShadowJar)
    ↓
Register transform<Target>ModJar (JarModificationTask)
    ↓
Configure modifications based on loader type
```

### 3. Build Execution

```
compileJava/compileGroovy
    ↓
shadowJar (Creates base JAR with relocated dependencies)
    ↓
For each target:
    shadow<Target>ModJar (Creates platform-specific JAR)
        ↓
    transform<Target>ModJar (Applies modifications)
        ├── JSON modifications (metadata files)
        ├── TOML modifications (Forge/NeoForge)
        ├── Annotation modifications (class files)
        └── Rename modifications (resource files)
    ↓
All artifacts ready for distribution
```

### 4. Publishing

```
transformModJar (All targets completed)
    ↓
publishToModrinth / publishToCurseForge
    ↓
For each target:
    Upload JAR with correct:
        ├── Minecraft version tag
        ├── Loader tags (fabric, forge, etc.)
        └── Release channel (alpha, beta, release)
```

## Key Design Patterns

### 1. **Plugin Pattern**
The main class implements Gradle's `Plugin<Project>` interface, integrating seamlessly with the Gradle ecosystem.

### 2. **Strategy Pattern**
Different modification strategies (`JsonModification`, `TomlModification`, `AnnotationModification`) implement the same `Modification` interface, allowing flexible composition.

### 3. **Builder Pattern**
The DSL-style configuration uses Gradle's property system to build up configuration objects incrementally.

### 4. **Chain of Responsibility**
Modifications are chained together, each processing relevant entries and passing through others.

### 5. **Factory Pattern**
The `setupTarget()` method acts as a factory, creating appropriate tasks and modifications based on the loader type.

### 6. **Immutable Data Objects**
Domain models (`Mod`, `Minecraft`) use `@Immutable` to prevent accidental modification and ensure thread safety.

## Code Organization

```
dev.huskuraft.universal.gradle/
├── UniversalPlugin.groovy           # Main plugin class
├── UniversalExtension.groovy        # DSL configuration
├── Mod.groovy                       # Mod metadata model
├── Loader.groovy                    # Loader enum
├── Environment.groovy               # Environment enum
├── task/
│   ├── JarModificationTask.java    # Core JAR transformation task
│   └── modification/
│       ├── Modification.java       # Base interface
│       ├── JsonModification.java   # JSON base class
│       ├── TomlModification.java   # TOML base class
│       ├── AnnotationModification.java  # ASM-based class modifications
│       ├── RenameModification.java # File path changes
│       ├── PlainTextModification.java   # Text replacements
│       ├── ClassModification.java  # Class file utilities
│       ├── fabric/                 # Fabric-specific modifications
│       │   ├── FabricModJsonPropertyModification.java
│       │   ├── FabricMixinsJsonPropertyModification.java
│       │   ├── FabricMixinsJsonRenameModification.java
│       │   ├── FabricRefmapJsonPropertyModification.java
│       │   ├── FabricRefmapJsonRenameModification.java
│       │   └── FabricAccessWidenerRenameModification.java
│       ├── forge/                  # Forge-specific modifications
│       │   ├── ForgeModTomlModification.java
│       │   └── ForgeAnnotationModification.java
│       └── neoforge/               # NeoForge-specific modifications
│           ├── NeoForgeModTomlModification.java
│           └── NeoForgeAnnotationModification.java
├── transformer/
│   └── JsonTransformer.groovy      # Shadow plugin JSON transformer
└── versioning/
    ├── Minecraft.groovy            # Minecraft version model
    └── VersionResolver.groovy      # Version lookup utility
```

## Working with the Codebase

### Adding a New Loader

To add support for a new mod loader:

1. **Add to Loader enum**:
```groovy
enum Loader {
    FABRIC, QUILT, FORGE, NEOFORGE, YOURLOADER
}
```

2. **Update API maps in UniversalPlugin**:
```groovy
private static Map<String, Object> API_MAP = [
    'your-loader-api': Loader.YOURLOADER
]

private static Map<String, Object> API_NAME_MAP = [
    'your-loader-api': 'YourLoader'
]
```

3. **Create modifications**:
```java
package dev.huskuraft.universal.gradle.task.modification.yourloader;

public class YourLoaderModification extends JsonModification {
    // Implementation
}
```

4. **Register in setupTarget()**:
```groovy
switch (API_MAP[api]) {
    case Loader.YOURLOADER:
        transformJarTargetTask.modification(new YourLoaderModification(mod))
        break
}
```

### Adding a New Modification Type

1. **Implement Modification interface**:
```java
public class CustomModification implements Modification {
    @Override
    public boolean appliesTo(JarEntry entry) {
        return entry.getName().endsWith(".custom");
    }
    
    @Override
    public byte[] apply(byte[] input) {
        // Transform content
        return transformed;
    }
}
```

2. **Register in platform setup**:
```groovy
transformJarTargetTask.modification(new CustomModification(mod))
```

### Extending Configuration

To add new configuration options:

1. **Add property to UniversalExtension**:
```groovy
@Input
abstract Property<String> getNewProperty()
```

2. **Handle in setupProperties()**:
```groovy
['mod.newProperty': extension.newProperty].forEach { property, value ->
    if (!value.present) value.set(project.properties.get(property) as String)
}
```

3. **Use in modifications**:
```groovy
mod.getNewProperty()
```

## Testing

The plugin includes a test suite using Spock Framework:

**Test Structure**:
```
src/test/groovy/
└── dev/huskuraft/universal/gradle/
    └── UniversalPluginTest.groovy
```

**Testing Approach**:
- Plugin application tests
- Configuration validation tests
- Task registration verification
- Modification application tests

## Dependencies

### Core Dependencies
- **Gradle API**: Plugin infrastructure
- **Groovy**: DSL and scripting
- **Shadow Plugin**: JAR shading and merging
- **ModPublish Plugin**: Publishing automation

### Transformation Libraries
- **ASM 9.5**: Bytecode manipulation for annotations
- **Gson 2.8.9**: JSON parsing and serialization
- **Night Config 3.6.7**: TOML/JSON configuration files

### Build Tools
- **Apache Ant 1.10.15**: ZIP/JAR utilities

## Configuration Examples

### Minimal Configuration
```groovy
universal {
    id = 'mymod'
    name = 'My Mod'
    description = 'A simple mod'
    authors = ['Me']
    license = 'MIT'
    environment = 'both'
    primaryUrl = uri('https://github.com/me/mymod')
    sourcesUrl = uri('https://github.com/me/mymod')
    supportUrl = uri('https://github.com/me/mymod/issues')
    
    targets = [
        ['1.20.1']: ['fabric-api']
    ]
}
```

### Full Configuration
```groovy
universal {
    id = 'mymod'
    name = 'My Universal Mod'
    description = 'Works everywhere!'
    authors = ['Developer1', 'Developer2']
    license = 'LGPLv3'
    environment = 'both'
    primaryUrl = uri('https://mymod.com')
    sourcesUrl = uri('https://github.com/me/mymod')
    supportUrl = uri('https://discord.gg/mymod')
    
    targets = [
        ['1.17.1']: ['fabric-api', 'forge-api'],
        ['1.18.2']: ['fabric-api', 'quilt-api', 'forge-api'],
        ['1.19.4']: ['fabric-api', 'quilt-api', 'forge-api'],
        ['1.20.1']: ['fabric-api', 'quilt-api', 'forge-api'],
        ['1.20.6']: ['fabric-api', 'neoforge-api'],
        ['1.21.1']: ['fabric-api', 'neoforge-api']
    ]
    
    modrinth {
        token = project.findProperty('modrinth.token')
        id = 'AbCdEfGh'
    }
    
    curseforge {
        token = project.findProperty('curseforge.token')
        id = '123456'
    }
}
```

### Using gradle.properties
```properties
group=com.example
version=1.0.0

mod.id=mymod
mod.name=My Mod
mod.description=A cool mod
mod.authors=Developer
mod.license=MIT
mod.environment=both
mod.primaryUrl=https://example.com
mod.sourcesUrl=https://github.com/example/mymod
mod.supportUrl=https://github.com/example/mymod/issues

mod.modrinth.id=AbCdEfGh
mod.modrinth.token=${MODRINTH_TOKEN}
mod.curseforge.id=123456
mod.curseforge.token=${CURSEFORGE_TOKEN}
```

## Common Tasks

### Build for All Targets
```bash
./gradlew build
```

### Build for Specific Target
```bash
./gradlew transform1201FabricModJar
```

### Publish to Modrinth
```bash
./gradlew publishToModrinth
```

### Publish to CurseForge
```bash
./gradlew publishToCurseForge
```

### Clean Build
```bash
./gradlew clean build
```

## Troubleshooting

### Common Issues

**Issue**: `common-api not found in implementation`
- **Solution**: Add `implementation 'dev.huskuraft.universal:common-api:VERSION'` to dependencies

**Issue**: `No universal targets found`
- **Solution**: Define `targets` map in `universal {}` configuration

**Issue**: Environment validation error
- **Solution**: Set `environment` to one of: `both`, `client`, or `server`

**Issue**: Modification not applying
- **Solution**: Check that the modification's `appliesTo()` matches the file path correctly

**Issue**: Publishing fails
- **Solution**: Verify tokens are set and have correct permissions

## Best Practices

1. **Version Consistency**: Keep common-api version synchronized across all dependencies
2. **Target Selection**: Only include targets you actively test and support
3. **Environment Setting**: Use `both` unless you have specific client/server-only features
4. **Secret Management**: Store tokens in environment variables, not in code
5. **Build Testing**: Test builds for each target before publishing
6. **Documentation**: Keep changelogs and documentation updated for multi-platform features

## Future Enhancements

Potential areas for expansion:
- Support for additional mod loaders (Rift, LiteLoader legacy support)
- Gradle Kotlin DSL support
- Incremental JAR modification for faster builds
- Parallel target building
- Automated compatibility testing
- Source set splitting for loader-specific code
- Custom modification DSL for user-defined transformations

## Contributing

When contributing to this project:
1. Follow existing code style (Groovy/Java conventions)
2. Add tests for new modifications
3. Update this document for architectural changes
4. Test with multiple Minecraft versions and loaders
5. Document any new configuration options

## License

Universal Gradle Plugin is licensed under **LGPLv3**.

---

**Last Updated**: November 17, 2025
**Plugin Version**: 0.3.0
**Minimum Gradle Version**: 7.0+
**Java Version**: 17+

