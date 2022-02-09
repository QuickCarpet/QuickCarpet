plugins {
    id("quickcarpet.java-conventions")
    id("fabric-loom")// version "0.10-SNAPSHOT"
    id("io.github.juuxel.loom-quiltflower-mini")// version "1.2.1"
}

dependencies {
    minecraft("com.mojang:minecraft:${findProperty("minecraft_version")}")
    mappings("net.fabricmc:yarn:${findProperty("yarn_mappings")}")
    modImplementation("net.fabricmc:fabric-loader:${findProperty("loader_version")}")
}

tasks.named<Copy>("processResources") {
    val properties = mapOf(
        "version" to rootProject.version,
        "api_version" to findProperty("api_version"),
        "malilib_version" to findProperty("malilib_version")
    )
    inputs.properties(properties)

    filesMatching("fabric.mod.json") {
        expand(properties)
    }
}