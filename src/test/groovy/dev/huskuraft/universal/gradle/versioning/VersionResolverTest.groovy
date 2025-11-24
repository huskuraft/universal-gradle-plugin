package dev.huskuraft.universal.gradle.versioning

import spock.lang.Specification
import spock.lang.Timeout

import java.util.concurrent.TimeUnit

/**
 * Simple practical tests that fetch real data from Mojang API
 */
class VersionResolverTest extends Specification {

    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    def "should fetch detailed version data from API"() {
        when:
        def version = VersionResolver.findDetailedById("1.21.4")

        then:
        version.isPresent()
        
        def v = version.get()
        v.id == "1.21.4"
        v.name == "1.21.4"
        v.type == "release"
        v.dataVersion == 4189
        v.protocolVersion == 769
        v.dataPackVersion == 61
        v.resourcePackVersion == 46
        v.buildTime != null
        v.stable == true
    }

    @Timeout(value = 120, unit = TimeUnit.SECONDS)
    def "should fetch data for multiple versions"() {
        when:
        def v1214 = VersionResolver.findDetailedById("1.21.4").get()
        def v1201 = VersionResolver.findDetailedById("1.20.1").get()
        def v1194 = VersionResolver.findDetailedById("1.19.4").get()
        def v1182 = VersionResolver.findDetailedById("1.18.2").get()
        def v1171 = VersionResolver.findDetailedById("1.17.1").get()

        then:
        // 1.21.4
        v1214.dataVersion == 4189
        v1214.protocolVersion == 769
        v1214.dataPackVersion == 61
        v1214.resourcePackVersion == 46
        
        // 1.20.1
        v1201.dataVersion == 3465
        v1201.protocolVersion == 763
        v1201.dataPackVersion == 15
        v1201.resourcePackVersion == 15
        
        // 1.19.4
        v1194.dataVersion == 3337
        v1194.protocolVersion == 762
        v1194.dataPackVersion == 12
        v1194.resourcePackVersion == 13
        
        // 1.18.2
        v1182.dataVersion == 2975
        v1182.protocolVersion == 758
        v1182.dataPackVersion == 9
        v1182.resourcePackVersion == 8
        
        // 1.17.1
        v1171.dataVersion == 2730
        v1171.protocolVersion == 756
        v1171.dataPackVersion == 7
        v1171.resourcePackVersion == 7
    }

    def "basic findById should work without detailed data"() {
        when:
        def version = VersionResolver.findById("1.21.4")

        then:
        version.isPresent()
        version.get().id == "1.21.4"
        version.get().type == "release"
        version.get().dataVersion == null
    }

    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    def "should handle older versions correctly"() {
        when:
        def v1165 = VersionResolver.findDetailedById("1.16.5").get()

        then:
        v1165.id == "1.16.5"
        v1165.dataVersion == 2586
        v1165.protocolVersion == 754
        v1165.dataPackVersion == 6
        v1165.resourcePackVersion == 6
        v1165.stable == true
    }

    def "should find versions by type"() {
        when:
        def allVersions = VersionResolver.getAllVersions()
        def releases = allVersions.values().findAll { it.type == "release" }
        def snapshots = allVersions.values().findAll { it.type == "snapshot" }

        then:
        !releases.isEmpty()
        !snapshots.isEmpty()
        releases.size() > 50
        snapshots.size() > 50
    }

    def "should return empty for non-existent version"() {
        when:
        def version = VersionResolver.findById("99.99.99")

        then:
        !version.isPresent()
    }

    def "should return empty for null version"() {
        when:
        def version = VersionResolver.findById(null)

        then:
        !version.isPresent()
    }
}
