# Universal Gradle Plugin

[Universal Gradle Plugin]() is a gradle plugin for building multiplatform mods for Minecraft.

## Usage

To use the Universal Gradle Plugin, follow these steps:

1. Add mod information to your `gradle.properties` file.

Required properties:

```properties
mod_id=universal
mod_name=Universal API
mod_license=LGPLv3
mod_version=0.1.0
mod_environment=both
mod_group_id=dev.huskuraft.universal
mod_authors=Huskcasaca
mod_description=A library for building multiplatform minecraft mods.
mod_display_url=https://github.com/huskuraft/universal-api
mod_sources_url=https://github.com/huskuraft/universal-api
mod_issues_url=https://github.com/huskuraft/universal-api/issues
```

Optional properties:

```properties
mod_modrinth_id=AbCdEfGh
mod_curseforge_id=123456
```

2. Add the plugin to your `build.gradle` file.

```groovy
plugins {
    id 'dev.huskuraft.universal' version '0.1.0'
}
```

3. Add common library using `implementation` and loader versions using `universalTarget` to your dependencies.

```groovy

dependencies {
    implementation 'dev.huskuraft.universal:common-api:0.1.0'

    [
        '1.17.1': ['fabric-api', 'quilt-api', 'forge-api',],
        '1.18.1': ['fabric-api', 'quilt-api', 'forge-api',],
        '1.18.2': ['fabric-api', 'quilt-api', 'forge-api',],
        '1.19.2': ['fabric-api', 'quilt-api', 'forge-api',],
        '1.19.3': ['fabric-api', 'quilt-api', 'forge-api',],
        '1.19.4': ['fabric-api', 'quilt-api', 'forge-api',],
        '1.20.1': ['fabric-api', 'quilt-api', 'forge-api',],
        '1.20.2': ['fabric-api', 'quilt-api', 'forge-api',],
        '1.20.4': ['fabric-api', 'quilt-api', 'forge-api',],
        '1.20.6': ['fabric-api', 'quilt-api', 'forge-api', 'neoforge-api'],
        '1.21.1': ['fabric-api', 'quilt-api', 'forge-api', 'neoforge-api'],
        '1.21.3': ['fabric-api', 'quilt-api', 'forge-api', 'neoforge-api'],
        //...
    ].forEach { minecraft, apis ->
        apis.forEach { api ->
            universalTarget "dev.huskuraft.universal:${api}:${minecraft}"
        }
    }
}
```
Targets can be found [here](https://github.com/huskuraft/universal-api#targets).

## License

Universal Gradle Plugin is licensed under LGPLv3.
