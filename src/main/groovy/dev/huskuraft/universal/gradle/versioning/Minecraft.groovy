package dev.huskuraft.universal.gradle.versioning

import com.google.gson.annotations.SerializedName
import groovy.transform.Immutable

@Immutable
class Minecraft {

    @SerializedName("id")
    String id

    @SerializedName("name")
    String name

    @SerializedName("release_target")
    String releaseTarget

    @SerializedName("type")
    String type

    @SerializedName("stable")
    boolean stable

    @SerializedName("data_version")
    int dataVersion

    @SerializedName("protocol_version")
    long protocolVersion

    @SerializedName("data_pack_version")
    int dataPackVersion

    @SerializedName("resource_pack_version")
    int resourcePackVersion

    @SerializedName("build_time")
    String buildTime

    @SerializedName("release_time")
    String releaseTime

    @SerializedName("sha1")
    String sha1

}
