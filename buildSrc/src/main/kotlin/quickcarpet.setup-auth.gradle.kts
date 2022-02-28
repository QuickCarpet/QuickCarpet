import quickcarpet.build.AuthHelper
import net.fabricmc.loom.LoomGradleExtension
import kotlin.system.exitProcess

plugins {
    id("fabric-loom")
}

tasks.register("setupAuth") {
    group = "ide"
    doLast {
        val auth = AuthHelper.setupAuth(file(".gradle/loom-cache/atlauncher"))
        if (auth == null) {
            System.err.println("Could not setup authentication")
            exitProcess(0)
        }
        updateDevLauncherConfig(auth["uuid"]!!, auth["accessToken"]!!, auth["username"]!!)
    }
}

fun updateDevLauncherConfig(uuid: String, accessToken: String, username: String) {
    val categories = linkedMapOf<String, MutableList<String>>()
    var category = mutableListOf<String>()
    val devLauncherConfig = LoomGradleExtension.get(project).files.devLauncherConfig
    println(devLauncherConfig)
    for (line in devLauncherConfig.readLines()) {
        if (!line.isEmpty() && Character.isWhitespace(line[0])) {
            category.add(line.trim())
        } else {
            category = mutableListOf()
            categories[line] = category
        }
    }
    val clientArgs = categories["clientArgs"]!!

    var i = 0
    while (i < clientArgs.size) {
        if (clientArgs[i] == "--accessToken" || clientArgs[i] == "--uuid" || clientArgs[i] == "--username" || clientArgs[i] == "--userType" || clientArgs[i] == "--userProperties") {
            clientArgs.removeAt(i)
            clientArgs.removeAt(i)
        } else {
            i += 2
        }
    }

    clientArgs.addAll(listOf(
        "--accessToken", accessToken,
        "--uuid", uuid,
        "--username", username
    ))

    val pw = devLauncherConfig.printWriter()
    for ((key, values) in categories) {
        pw.println(key)
        for (v in values) {
            pw.println("\t" + v)
        }
    }
    pw.flush()
}