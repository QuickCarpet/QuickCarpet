![QuickCarpet Icon](src/template/resources/quickcarpet/icon@128.png)

# QuickCarpet
![GitHub](https://img.shields.io/github/license/DeadlyMC/QuickCarpet?style=flat-square)
![GitHub commits since latest release (by date)](https://img.shields.io/github/commits-since/DeadlyMC/QuickCarpet/latest/master?style=flat-square)
![GitHub commits since latest release (by date including pre-releases)](https://img.shields.io/github/commits-since/DeadlyMC/QuickCarpet/latest/master?include_prereleases&style=flat-square)

This is not the [official carpet version](https://github.com/gnembon/fabric-carpet) by [gnembon](https://github.com/gnembon),
but an alternative implementation providing a different set of features.

Major features include:
- Commands to measure performance and change the tick speed
- Movable Block Entities
- Auto Crafting Table
- Features to make more items renewable

Almost all features can be configured using `/carpet` and are set to their vanilla value by default.

A list of all configurable rules can be queried with `/carpet list` or seen
[here][rules] for the latest development version

Primarily aimed for use as a standalone mod, but compatibility with other *open source* mods is attempted if feasible.
Report an [issue][new-issue] in that case.

## Installation
- Install Fabric: [Instructions][fabric-wiki-install] 
- Download QuickCarpet:
    - Full releases from [Releases][releases]
    - Development builds from [Actions][actions-dev-builds]
        - Select the build you want to download
            (`master` for the latest stable Minecraft version,other branches for snapshots)
        - When logged in click on `quickcarpet-jars`
        - Extract `quickcarpet-jars` and use the JAR file with the shortest name (without `-dev.jar` or `-sources`)
- Put the `quickcarpet-<version>.jar` into the `mods/` folder

## Compiling
Steps 2 & 3 are optional if you don't want to change the source code

1. Clone this repo
2. Run `gradlew genSources idea` for IntelliJ and `gradlew genSources eclipse` for Eclipse and import the Gradle project
3. Run `gradlew generateJava` and add `build/generated/java` as a source directory ("Generated Sources Root" in IntelliJ)
4. Run `gradlew build` for creating a build (result in `build/libs/`)

[rules]: rules.md
[new-issue]: ../../issues/new
[releases]: ../../releases
[actions-dev-builds]: ../../actions?query=workflow%3A%22Development+Builds%22
[fabric-wiki-install]: https://fabricmc.net/wiki/install