package dev.huskuraft.universal.gradle

import spock.lang.Specification

class UniversalPluginTest extends Specification {

    def "extractReleaseType should return the correct release type for valid semantic versions"() {
        expect:
        UniversalPlugin.extractReleaseType(version) == expectedReleaseType

        where:
        version               | expectedReleaseType
        "1.0.0-alpha.1"       | "alpha"
        "2.3.4-beta"          | "beta"
        "3.0.0-rc.2"          | "rc"
        "4.5.6-pre.3"         | "pre"
        "5.6.7-dev"           | "dev"
        "6.7.8-snapshot"      | "snapshot"
        "7.8.9-alpha"         | "alpha"
        "8.9.10-beta.5"       | "beta"
        "9.10.11-rc.10"       | "rc"
    }

    def "extractReleaseType should return null for versions without a release type"() {
        expect:
        UniversalPlugin.extractReleaseType(version) == null

        where:
        version               | _
        "1.0.0"               | _
        "2.3.4"               | _
        "3.0.0"               | _
        "4.5.6"               | _
        "5.6.7"               | _
    }

    def "extractReleaseType should return null for invalid version formats"() {
        expect:
        UniversalPlugin.extractReleaseType(version) == null

        where:
        version               | _
        "invalid-version"     | _
        "alpha.1"             | _
        "beta"                | _
        "rc.2"                | _
        "pre.3"               | _
        "dev"                 | _
        "snapshot"            | _
    }
}
