/**
 * @author Earthcomputer
 * To use your real account in the IDE, do the following:
 * - Create a credentials.properties file in the root project directory if it doesn't already exist (it is ignored by the gitignore)
 * - Insert minecraftUser=youremail@example.com and minecraftPass=yourminecraftpassword into the credentials file
 * - Duplicate the Minecraft Client run configuration in your IDE. Configure it to run the setupAuth gradle task after build but before run
 */

import com.google.gson.GsonBuilder
import com.mojang.authlib.Agent
import com.mojang.authlib.properties.PropertyMap
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
import net.fabricmc.loom.LoomGradleExtension
import java.util.Properties
import java.io.FileReader
import java.net.Proxy

plugins {
    id("fabric-loom")
}

val credentialsFile = file("credentials.properties")
if (credentialsFile.exists()) {
    tasks.register("setupAuth") {
        group = "ide"
        doLast {
            val credentials = Properties()
            credentials.load(FileReader(file("credentials.properties")))
            var username = credentials["minecraftUser"].toString()
            val password = credentials["minecraftPass"].toString()
            val auth = YggdrasilAuthenticationService(Proxy.NO_PROXY, "1").createUserAuthentication(Agent.MINECRAFT)
            auth.setUsername(username)
            auth.setPassword(password)
            auth.logIn()
            val accessToken = auth.authenticatedToken
            val uuid = auth.selectedProfile.id.toString().replace("-", "")
            username = auth.selectedProfile.name
            val userType = auth.userType.name
            val userProperties = GsonBuilder().registerTypeAdapter(PropertyMap::class.java, PropertyMap.Serializer()).create().toJson(auth.userProperties)

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
                "--username", username,
                "--userType", userType,
                "--userProperties", userProperties
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
    }
}
