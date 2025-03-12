# Universal Gradle Plugin

[Universal Gradle Plugin]() is a gradle plugin for building multiplatform mods for Minecraft.

## Usage

To use the Universal Gradle Plugin, follow these steps:

1. Add the plugin to your `build.gradle` file.

```groovy
plugins {
    id 'dev.huskuraft.universal' version '0.1.0'
}
```

2. Add common library using `implementation` to your dependencies.

```groovy
dependencies {
    implementation 'dev.huskuraft.universal:common-api:0.1.0'
}
```

3. Add project and mod information to `universal {}` configuration or `gradle.properties` file.

Required properties:

```groovy
group = 'dev.huskuraft.universal'
version = '0.0.0'

universal {
    id = 'universal'
    name = 'Universal API'
    description = 'A library for building multiplatform minecraft mods.'
    authors = ['Huskcasaca']
    license = 'LGPLv3'
    environment = 'both'
    primaryUrl = uri('https://github.com/huskuraft/universal-api')
    sourcesUrl = uri('https://github.com/huskuraft/universal-api')
    supportUrl = uri('https://github.com/huskuraft/universal-api/issues')
}
```

```properties
group=dev.huskuraft.universal
version=0.0.0

mod.id=universal
mod.name=Universal API
mod.description=A library for building multiplatform minecraft mods.
mod.authors=Huskcasaca
mod.license=LGPLv3
mod.environment=both
mod.primaryUrl=https://github.com/huskuraft/universal-api
mod.sourcesUrl=https://github.com/huskuraft/universal-api
mod.supportUrl=https://github.com/huskuraft/universal-api/issues
```

Optional properties:

```groovy
universal {
    modrinth {
        token = ''
        id = 'AbCdEfGh'
    }
    curseforge {
        token = ''
        id = '123456'
    }
}
```

```properties
mod.modrinth.id=AbCdEfGh
mod.modrinth.token=
mod.curseforge.id=123456
mod.curseforge.token=
```

4. Add targets of Minecraft and APIs to `universal {}` configuration.

```groovy
universal {
    targets = [
        ['1.17.1']: ['fabric-api', 'quilt-api', 'forge-api'],
        ['1.18.1']: ['fabric-api', 'quilt-api', 'forge-api'],
        ['1.18.2']: ['fabric-api', 'quilt-api', 'forge-api'],
        ['1.19.2']: ['fabric-api', 'quilt-api', 'forge-api'],
        ['1.19.3']: ['fabric-api', 'quilt-api', 'forge-api'],
        ['1.19.4']: ['fabric-api', 'quilt-api', 'forge-api'],
        ['1.20.1']: ['fabric-api', 'quilt-api', 'forge-api'],
        ['1.20.2']: ['fabric-api', 'quilt-api', 'forge-api'],
        ['1.20.4']: ['fabric-api', 'quilt-api', 'forge-api'],
        ['1.20.6']: ['fabric-api', 'quilt-api', 'forge-api', 'neoforge-api'],
        ['1.21.1']: ['fabric-api', 'quilt-api', 'forge-api', 'neoforge-api'],
        ['1.21.3']: ['fabric-api', 'quilt-api', 'forge-api', 'neoforge-api'],
    ]
}
```

Targets can be found [here](https://github.com/huskuraft/universal-api#targets).

## License

Universal Gradle Plugin is licensed under LGPLv3.
