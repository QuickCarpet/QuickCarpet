@file:Suppress("UnstableApiUsage")

rootProject.name = "quickcarpet"

include("quickcarpet-api")
project(":quickcarpet-api").projectDir = file("api")

dependencyResolutionManagement {
    versionCatalogs {
        create("mods") {
            from(files("mods.versions.toml"))
        }
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}