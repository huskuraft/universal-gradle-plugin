package dev.huskuraft.universal.gradle.versioning

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration
import java.time.Instant

/**
 * Resolves Minecraft version information from Mojang's API.
 * 
 * File structure:
 *   ~/.gradle/caches/universal-gradle-plugin/
 *   ├── version_manifest.json           (cached manifest, 24h TTL)
 *   ├── version-detail-<version>.json   (detailed version data)
 *   └── tmp/                            (temporary JAR downloads, auto-cleaned)
 */
class VersionResolver {

    private static final String MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json"
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create()
    private static final Duration CACHE_DURATION = Duration.ofHours(24)
    private static final Path cacheDir = Paths.get(System.getProperty("user.home"), ".gradle", "caches", "universal-gradle-plugin")
    private static final Path manifestCache = cacheDir.resolve("version_manifest.json")
    
    private static Map<String, Minecraft> versionCache = null
    private static Instant lastFetchTime = null

    static Optional<Minecraft> findById(String id) {
        ensureVersionsLoaded()
        def version = versionCache?.get(id)
        return version ? Optional.of(version) : Optional.empty()
    }

    static Optional<Minecraft> findDetailedById(String id) {
        def basic = findById(id)
        if (!basic.isPresent() || !basic.get().url) {
            return Optional.empty()
        }

        try {
            return Optional.of(fetchDetailedVersion(id, basic.get()))
        } catch (Exception e) {
            println "Warning: Could not fetch detailed version for ${id}: ${e.message}"
            return basic
        }
    }

    static Map<String, Minecraft> getAllVersions() {
        ensureVersionsLoaded()
        return Collections.unmodifiableMap(versionCache ?: [:])
    }

    static synchronized void refresh() {
        versionCache = null
        lastFetchTime = null
        loadVersions()
    }

    private static synchronized void ensureVersionsLoaded() {
        if (versionCache && lastFetchTime) {
            def age = Duration.between(lastFetchTime, Instant.now())
            if (age < CACHE_DURATION) return
        }
        loadVersions()
    }

    private static void loadVersions() {
        try {
            versionCache = loadVersionsFromSource(MANIFEST_URL)
            lastFetchTime = Instant.now()
            saveToCache()
        } catch (Exception e) {
            try {
                versionCache = loadVersionsFromSource(manifestCache.toUri().toString())
                lastFetchTime = Instant.now()
                println "Warning: Using cached data: ${e.message}"
            } catch (Exception cacheError) {
                throw new RuntimeException("Failed to load versions", cacheError)
            }
        }
    }

    private static Map<String, Minecraft> loadVersionsFromSource(String source) {
        def manifest = source.startsWith("http") ? fetchJson(source) : 
                      gson.fromJson(Files.readString(Paths.get(new URI(source))), JsonObject.class)
        def versions = manifest.getAsJsonArray("versions")
        
        return versions.collectEntries { versionElement ->
            def v = versionElement.getAsJsonObject()
            def id = v.get("id").getAsString()
            [(id): new Minecraft(
                id: id,
                name: id,
                type: v.get("type").getAsString(),
                url: v.get("url").getAsString(),
                time: v.get("time").getAsString(),
                releaseTime: v.get("releaseTime").getAsString(),
                sha1: null, releaseTarget: null, stable: null,
                dataVersion: null, protocolVersion: null,
                dataPackVersion: null, resourcePackVersion: null,
                buildTime: null, complianceLevel: null
            )]
        }
    }

    private static void saveToCache() {
        try {
            Files.createDirectories(cacheDir)
            Files.writeString(manifestCache, fetchJsonString(MANIFEST_URL))
        } catch (Exception e) {
            println "Warning: Could not save cache: ${e.message}"
        }
    }

    private static Minecraft fetchDetailedVersion(String id, Minecraft basic) {
        def detailCache = cacheDir.resolve("version-detail-${id}.json")
        
        // Try cache first
        if (Files.exists(detailCache)) {
            try {
                return parseFromCache(Files.readString(detailCache), basic)
            } catch (Exception ignored) {}
        }

        // Fetch from API
        def versionMeta = fetchJson(basic.url)
        def clientUrl = versionMeta.getAsJsonObject("downloads")
                                   .getAsJsonObject("client")
                                   .get("url").getAsString()
        
        // Store temp JARs in Gradle cache directory (not system temp)
        def tmpDir = cacheDir.resolve("tmp")
        Files.createDirectories(tmpDir)
        def jarFile = tmpDir.resolve("${id}-client.jar")
        
        try {
            downloadFile(clientUrl, jarFile)
            def versionJson = extractVersionJson(jarFile)
            def detailed = parseFromJar(id, versionJson, basic)
            
            // Save to cache
            try {
                Files.writeString(detailCache, gson.toJson(detailed))
            } catch (Exception ignored) {}
            
            return detailed
        } finally {
            // Delete JAR immediately after extraction (20-40MB each)
            // The extracted data is cached in version-detail-*.json
            try { Files.deleteIfExists(jarFile) } catch (Exception ignored) {}
        }
    }

    private static Minecraft parseFromJar(String id, String jsonContent, Minecraft basic) {
        def json = gson.fromJson(jsonContent, JsonObject.class)
        
        def packVersion = json.has("pack_version") ? json.get("pack_version") : null
        def dataPack = null, resourcePack = null
        
        if (packVersion?.isJsonPrimitive()) {
            dataPack = resourcePack = packVersion.getAsInt()
        } else if (packVersion?.isJsonObject()) {
            def pack = packVersion.getAsJsonObject()
            dataPack = pack.has("data") ? pack.get("data").getAsInt() : 
                      (pack.has("data_major") ? pack.get("data_major").getAsInt() : null)
            resourcePack = pack.has("resource") ? pack.get("resource").getAsInt() :
                          (pack.has("resource_major") ? pack.get("resource_major").getAsInt() : null)
        }
        
        def javaVersion = json.has("java_version") && json.get("java_version").isJsonObject() ?
                         json.getAsJsonObject("java_version") : null
        
        return new Minecraft(
            id: id,
            name: getStringOrNull(json, "name") ?: id,
            type: basic.type,
            url: basic.url,
            time: basic.time,
            releaseTime: basic.releaseTime,
            sha1: basic.sha1,
            releaseTarget: getStringOrNull(json, "release_target"),
            stable: json.has("stable") ? json.get("stable").getAsBoolean() : true,
            dataVersion: getIntOrNull(json, "world_version"),
            protocolVersion: getLongOrNull(json, "protocol_version"),
            dataPackVersion: dataPack,
            resourcePackVersion: resourcePack,
            buildTime: getStringOrNull(json, "build_time"),
            complianceLevel: javaVersion?.has("major_version") ? javaVersion.get("major_version").getAsInt() : null
        )
    }

    private static Minecraft parseFromCache(String jsonString, Minecraft basic) {
        def json = gson.fromJson(jsonString, JsonObject.class)
        return new Minecraft(
            id: getStringOrNull(json, "id") ?: basic.id,
            name: getStringOrNull(json, "name") ?: basic.id,
            type: getStringOrNull(json, "type") ?: basic.type,
            url: getStringOrNull(json, "url") ?: basic.url,
            time: getStringOrNull(json, "time") ?: basic.time,
            releaseTime: getStringOrNull(json, "releaseTime") ?: basic.releaseTime,
            sha1: getStringOrNull(json, "sha1"),
            releaseTarget: getStringOrNull(json, "release_target"),
            stable: json.has("stable") ? json.get("stable").getAsBoolean() : null,
            dataVersion: getIntOrNull(json, "data_version"),
            protocolVersion: getLongOrNull(json, "protocol_version"),
            dataPackVersion: getIntOrNull(json, "data_pack_version"),
            resourcePackVersion: getIntOrNull(json, "resource_pack_version"),
            buildTime: getStringOrNull(json, "build_time"),
            complianceLevel: getIntOrNull(json, "compliance_level")
        )
    }

    private static String getStringOrNull(JsonObject json, String key) {
        json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null
    }

    private static Integer getIntOrNull(JsonObject json, String key) {
        json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsInt() : null
    }

    private static Long getLongOrNull(JsonObject json, String key) {
        json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsLong() : null
    }

    private static JsonObject fetchJson(String url) {
        gson.fromJson(fetchJsonString(url), JsonObject.class)
    }

    private static String fetchJsonString(String urlString) {
        def connection = new URL(urlString).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10000
        connection.readTimeout = 10000
        connection.setRequestProperty("User-Agent", "UniversalGradlePlugin/1.0")
        
        try {
            if (connection.responseCode != 200) {
                throw new IOException("HTTP ${connection.responseCode}")
            }
            return connection.inputStream.text
        } finally {
            connection.disconnect()
        }
    }

    private static void downloadFile(String url, Path destination) {
        def connection = new URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "UniversalGradlePlugin/1.0")
        connection.connectTimeout = 10000
        connection.readTimeout = 60000
        
        try {
            if (connection.responseCode != 200) {
                throw new IOException("HTTP ${connection.responseCode}")
            }
            destination.toFile().withOutputStream { it << connection.inputStream }
        } finally {
            connection.disconnect()
        }
    }

    private static String extractVersionJson(Path jarPath) {
        def zipFile = new java.util.zip.ZipFile(jarPath.toFile())
        try {
            def entry = zipFile.getEntry("version.json")
            if (!entry) throw new FileNotFoundException("version.json not found")
            return zipFile.getInputStream(entry).text
        } finally {
            zipFile.close()
        }
    }
}
