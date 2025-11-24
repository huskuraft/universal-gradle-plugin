package dev.huskuraft.universal.gradle.versioning

import com.google.gson.annotations.SerializedName
import groovy.transform.Immutable

@Immutable
class Minecraft {

    @SerializedName("id")
    String id

    @SerializedName("name")
    String name

    @SerializedName("type")
    String type

    @SerializedName("url")
    String url

    @SerializedName("time")
    String time

    @SerializedName("releaseTime")
    String releaseTime

    @SerializedName("sha1")
    String sha1

    // Extended properties from version.json inside JAR
    @SerializedName("release_target")
    String releaseTarget

    @SerializedName("stable")
    Boolean stable

    @SerializedName("data_version")
    Integer dataVersion

    @SerializedName("protocol_version")
    Long protocolVersion

    @SerializedName("data_pack_version")
    Integer dataPackVersion

    @SerializedName("resource_pack_version")
    Integer resourcePackVersion

    @SerializedName("build_time")
    String buildTime

    @SerializedName("compliance_level")
    Integer complianceLevel

}
