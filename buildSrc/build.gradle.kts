
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
        url = uri("https://libraries.minecraft.net/")
    }
    gradlePluginPortal()
}

dependencies {
    implementation("net.fabricmc:fabric-loom:0.10-SNAPSHOT")
    implementation("io.github.juuxel:loom-quiltflower-mini:1.2.1")
    implementation("com.mojang:authlib:3.2.38")
    implementation("com.google.guava:guava:31.0.1-jre")
    implementation("com.google.code.gson:gson:2.8.8")
}