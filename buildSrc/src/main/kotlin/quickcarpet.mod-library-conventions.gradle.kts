plugins {
    id("quickcarpet.mod-conventions")
    `java-library`
    `maven-publish`
}

val ENV = System.getenv()

configurations {
    create("dev")
}

artifacts {
    add("dev", tasks.jar)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }

    repositories {
        if (ENV["MAVEN_URL"] != null) {
            maven {
                url = uri(ENV["MAVEN_URL"]!!)
                credentials {
                    username = ENV["MAVEN_USERNAME"]!!
                    password = ENV["MAVEN_PASSWORD"]!!
                }
            }
        }
    }
}