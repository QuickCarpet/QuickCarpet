import org.gradle.api.tasks.compile.JavaCompile;

plugins {
    java
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    maven {
        name = "Masa"
        url = uri("https://masa.dy.fi/maven/")
    }
    maven {
        name = "QuiltMC"
        url = uri("https://maven.quiltmc.org/repository/release/")
    }
    maven {
        name = "TerraformersMC"
        url = uri("https://maven.terraformersmc.com/releases/")
    }
}

dependencies {
    // javax.annotation.Nullable/Nonnull
    compileOnly("com.google.code.findbugs:jsr305:3.0.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    from("${rootDir}/LICENSE.md")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(17)
}

task<Jar>("sourcesJar") {
    dependsOn(tasks.classes)
    classifier = "sources"
    from(sourceSets.main.get().allSource)
}