# QuickCarpet114
Carpetmod for snapshot testing!
This is not the official carpet vesion.
Built on top of the Fabric modding framework.
Meant to be used as a standalone mod! Compatibility with other mods is not tested.

## Requirements
- Fabric Installer : https://fabricmc.net/use/

## How to install?
Singleplayer :
- Download fabric installer for client for appropriate version.
- Open the installer and make sure you are in the client tab.
- Fill all options and hit install.
- Download the mod jar.
- Place the jar file in your mods folder. (Make sure u put the mods directly inside `mods/` and not in
  something like mods/1.14)
- Open the minecraft launcher and run the game with fabric profile.

Multiplayer :
- Download the fabric installer for server for the appropriate version.
- Open the installer and go to server tab.
- Fill all options and hit install.
- You should now have a `fabric-server-launch.jar`. Place it in the folder will all the mod files.
- Run the `fabric-server-launch.jar` once, it should create a `fabric-server-launch.properties` file.
  Specify the name of the 1.14 server jar in this file.
- Create a folder `mods` in the same directory and place the mod jar.
- Run the `fabric-server-launch.jar` to launch the server.

## Compiling
- Clone this repo
- Run `gradlew genSources idea` for IntelliJ and `gradlew genSources eclipse` for Eclipse
- Add `build/generated/java` as a source directory ("Generated Sources Root" in IntelliJ)
- Run `gradlew build` for creating local jars
