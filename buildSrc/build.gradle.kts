
plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    maven {
        name = "Quilt Snapshot"
        url = uri("https://maven.quiltmc.org/repository/snapshot/")
    }
    maven {
        name = "Quilt Release"
        url = uri("https://maven.quiltmc.org/repository/release/")
    }
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
    maven {
        name = "Cotton"
        url = uri("https://server.bbkr.space/artifactory/libs-release/")
    }
    maven {
        name = "skyrising"
        url = uri("https://maven.skyrising.xyz/")
    }
    gradlePluginPortal()
}

dependencies {
    implementation("org.quiltmc:loom:0.12.23")
    implementation("com.atlauncher:ATLauncher:3.4.12.2")
}