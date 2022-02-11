plugins {
    id("quickcarpet.java-conventions")
    id("fabric-loom")
    id("io.github.juuxel.loom-quiltflower-mini")
}

val mods = project.extensions.getByType<VersionCatalogsExtension>().named("mods")
val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    minecraft(libs.findLibrary("minecraft").get())
    mappings(variantOf(libs.findLibrary("yarn").get()) {
        classifier("v2")
    })
    modImplementation(libs.findLibrary("loader").get())
}

tasks.named<Copy>("processResources") {
    val properties = mapOf(
        "version" to rootProject.version,
        "api_version" to mods.findVersion("quickcarpet-api").get(),
        "malilib_version" to libs.findVersion("malilib").get()
    )
    inputs.properties(properties)

    filesMatching("fabric.mod.json") {
        expand(properties)
    }
}