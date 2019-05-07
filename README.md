# QuickCarpet114
Carpetmod for snapshot testing!
This is not the official carpet vesion.
Built on top of the Fabric modding framework.
Meant to be used as a standalone mod! Compatibility with other mods is not tested.

## Requirements
- Fabric API (Only for skyblock version) : https://minecraft.curseforge.com/projects/fabric
- Fabric Installer : https://fabricmc.net/use/

## How to install?
Singleplayer :
1. Download fabric installer for client for appropriate version.
2. Open the installer and make sure you are in the client tab.
3. Fill all options and hit install.
4. Download the mod jar and Fabric API (if required).
5. Place both the jars in your mods folder. (Make sure u put the mods directly inside `mods/` and not in
   something like mods/1.14)
6. Open the minecraft launcher and run the game with fabric profile.

Multiplayer :
1. Download the fabric installer for server for the appropriate version.
2. Open the installer and go to server tab.
3. Fill all options and hit install.
4. You should now have a `fabric-server-launch.jar`. Place it in the folder will all the mod files.
5. Run the `fabric-server-launch.jar` once, it should create a `fabric-server-launch.properties` file.
   Specify the name of the 1.14 server jar in this file.
6. Create a folder `mods` in the same directory and place the mod and API(if required).
7. Run the `fabric-server-launch.jar` to launch the server.

## Compiling
- Clone this repo
- Run `gradlew genSources idea` for Intellij and `gradlew genSources eclipse` for Eclipse
- Run `gradlew build` for creating local jars
