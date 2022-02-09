import java.text.SimpleDateFormat
import quickcarpet.build.*
import java.util.*

plugins {
	id("quickcarpet.mod-conventions")
	id("quickcarpet.setup-auth")
}

allprojects {
	group = "quickcarpet"
}

loom {
	accessWidenerPath.set(file("src/main/resources/quickcarpet.accesswidener"))
	runs {
		create("testServer") {
			server()
			source("test")
			name("Minecraft Test Server")
			vmArgs("-Dquickcarpet.test=true")
			programArgs("${rootDir}/src/test/resources/structures")
		}
	}
}

tasks.check {
	dependsOn(tasks["runTestServer"])
}

val buildDate = Date()
val branch = GitHelper.getBranch(rootDir)
val ver = GitHelper.getVersion(rootDir, findProperty("mod_version") as String)

if (branch != "master" && branch != "main" && branch != "HEAD" && ver.pre.isNotEmpty()) {
	ver.pre.add(0, branch.replace(Regex("[+-]"), "_"))
}

println(ver)

version = ver.toString()

val malilib_mc_version = findProperty("malilib_mc_version") ?: findProperty("minecraft_version")

dependencies {
	implementation(project(path = ":quickcarpet-api", configuration = "dev"))
	include(project(":quickcarpet-api"))

	modImplementation("net.fabricmc.fabric-api:fabric-resource-loader-v0:${findProperty("fabric_resource_loader_v0_version")}")
	//modImplementation("net.fabricmc.fabric-api:fabric-networking-api-v1:${findProperty("fabric_networking_api_v1_version"}")
	//modImplementation("net.fabricmc.fabric-api:fabric-registry-sync-v0:${findProperty("fabric_registry_sync_v0_version"}")
	modCompileOnly("net.fabricmc.fabric-api:fabric-api:${findProperty("fabric_version")}")
	modCompileOnly("com.terraformersmc:modmenu:${findProperty("modmenu_version")}")

	include("net.fabricmc.fabric-api:fabric-resource-loader-v0:${findProperty("fabric_resource_loader_v0_version")}")

	modCompileOnly("fi.dy.masa.malilib:malilib-fabric-${malilib_mc_version}:${findProperty("malilib_version")}") {
		exclude("modmenu")
	}
}

task<Copy>("generateJava") {
	group = "build"
	description = "Generates Build.java"

	val templateContext = mapOf(
		"version" to project.version,
		"timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").apply {
			timeZone = TimeZone.getTimeZone("UTC")
		}.format(buildDate),
		"branch" to GitHelper.getBranch(rootDir),
		"commit" to GitHelper.getCommit(rootDir),
		"working_dir_clean" to GitHelper.getStatus(rootDir),
		"minecraft_version" to findProperty("minecraft_version"),
		"yarn_mappings" to findProperty("yarn_mappings")
	)
	inputs.properties(templateContext) // for gradle up-to-date check
	from("src/template/java")
	into("$buildDir/generated/java")
	expand(templateContext)
}

sourceSets.main.get().java.srcDir("$buildDir/generated/java")
tasks.compileJava {
	dependsOn(tasks["generateJava"])
}

task<JavaExec>("dumpRules") {
	group = JavaBasePlugin.DOCUMENTATION_GROUP
	description = "Writes all rules to rules.md"

	inputs.file("src/main/java/quickcarpet/settings/Settings.java")
	inputs.file("src/main/resources/assets/quickcarpet/lang/en_us.json")
	outputs.file("rules.md")
	outputs.file("run/rules.md")
	dependsOn(tasks.classes)

	workingDir("run")
	classpath = sourceSets.main.get().runtimeClasspath
	mainClass.set("quickcarpet.settings.Settings")
	doLast {
		copy {
			from("run/rules.md")
			into(".")
		}
	}
}

gradle.taskGraph.whenReady {
	tasks["generateJava"].enabled = !gradle.taskGraph.hasTask(tasks["dumpRules"])
}