
plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
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
    implementation("net.fabricmc:fabric-loom:0.12-SNAPSHOT")
    //implementation("io.github.juuxel:loom-quiltflower-mini:1.2.1")
    implementation("com.atlauncher:ATLauncher:3.4.12.2")
}