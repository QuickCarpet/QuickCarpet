# Quick Carpet Rules
## accurateBlockPlacement
Allows client mods to specify the orientation of placed blocks

Type: `boolean`  
Default: `true`  
Options: `true`, `false`  
Categories: feature  

## autoCraftingTable
Automatic crafting table

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: feature, experimental  

## commandCameramode
Enables /c and /s commands to quickly switch between camera and survival modes for players with this permission level

Type: `int`  
Default: `2`  
Categories: commands  
Validator: `OP Level (0-4)`  

## commandCarpetClone
Enables /carpetclone command for players with this permission level

This is a replica of the /clone command for fillUpdates and fillLimits  
Type: `int`  
Default: `2`  
Categories: commands  
Validator: `OP Level (0-4)`  

## commandCarpetFill
Enables /carpetfill command for players with this permission level

This is a replica of the /fill command for fillUpdates and fillLimits  
Type: `int`  
Default: `2`  
Categories: commands  
Validator: `OP Level (0-4)`  

## commandCarpetSetBlock
Enables /carpetsetblock command for players with this permission level

This is a replica of the /setblock command for fillUpdates and fillLimits  
Type: `int`  
Default: `2`  
Categories: commands  
Validator: `OP Level (0-4)`  

## commandLog
Enables /log command to monitor events in the game via chat and overlays for players with this permission level

Type: `int`  
Default: `0`  
Categories: commands  
Validator: `OP Level (0-4)`  

## commandMeasure
Enables /measure command for measuring distances for players with this permission level

Type: `int`  
Default: `0`  
Categories: commands  
Validator: `OP Level (0-4)`  

## commandPing
Enables /ping for players to get their ping for players with this permission level

Type: `int`  
Default: `0`  
Categories: commands  
Validator: `OP Level (0-4)`  

## commandPlayer
Enables /player command to control/spawn players for players with this permission level

Type: `int`  
Default: `0`  
Categories: commands  
Validator: `OP Level (0-4)`  

## commandSpawn
Enables /spawn command for spawn tracking for players with this permission level

Type: `int`  
Default: `0`  
Categories: commands  
Validator: `OP Level (0-4)`  

## commandTick
Enables /tick command to analyze game speed for players with this permission level

Type: `int`  
Default: `0`  
Categories: commands  
Validator: `OP Level (0-4)`  

## commandTickManipulate
Allows the tick rate modifications of /tick for players with this permission level

Values lower then commandTick have no effect  
Type: `int`  
Default: `2`  
Categories: commands  
Validator: `OP Level (0-4)`  

## commandWaypoint
Enables /waypoint command for managing waypoints for players with this permission level and /tp [entities] waypoint (for permission level 2)

Type: `int`  
Default: `0`  
Categories: commands  
Validator: `OP Level (0-4)`  

## dispensersBreakBlocks
Gives dispensers the ability to break blocks using gunpowder

Type: `Option`  
Default: `false`  
Options: `false`, `normal`, `silk_touch`  
Categories: feature, experimental  

## dispensersPlaceBlocks
Dispensers can place most blocks

Type: `Option`  
Default: `false`  
Options: `false`, `whitelist`, `blacklist`, `all`  
Categories: experimental, feature  

## dispensersShearVines
carpet.rule.dispensersShearVines.description

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: feature  

## dispensersTillSoil
Dispensers with hoes can till soil

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: feature  

## doubleRetraction
1.8 double retraction from pistons.

Gives pistons the ability to double retract without side effects.  
Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: experimental  
Fixes: [MC-88959](https://bugs.mojang.com/browse/MC-88959)  

## explosionNoBlockDamage
Explosions won't destroy blocks

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: tnt  

## fillLimit
Customizable fill/clone volume limit

Type: `int`  
Default: `32768`  
Options: `32768`, `250000`, `1000000`  
Categories: creative  
Validator: `> 0`  

## fillUpdates
fill/clone/setblock and structure blocks cause block updates

Type: `boolean`  
Default: `true`  
Options: `true`, `false`  
Categories: creative  

## fireChargeConvertsToNetherrack
Fire charges from dispensers convert cobblestone to netherrack

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: feature, experimental  

## flippinCactus
Players can flip and rotate blocks when holding cactus

Doesn't cause block updates when rotated/flipped  
Applies to pistons, observers, droppers, repeaters, stairs, glazed terracotta etc.  
Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: creative, survival  

## hopperCounters
Hoppers pointing to wool will count items passing through them

Enables /counter command, and actions while placing red and green carpets on wool blocks  
Use /counter <color?> reset to reset the counter, and /counter <color?> to query  
Counters are global and shared between players, 16 channels available  
Items counted are destroyed, count up to one stack per tick per hopper  
Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: commands  

## isDevelopment
Sets the isDevelopment constant

For example enables the /test command  
Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: experimental  

## mobInFireConvertsSandToSoulsand
If a living entity dies on sand with fire on top the sand will convert into soul sand

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: feature, experimental  

## movableBlockEntities
Pistons can push block entities, like hoppers, chests etc.

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: feature, experimental  

## movableBlockOverrides
Override how pistons interact with any block. Adds weak stickyness behavior used by default for redstone components, flowerpots, etc.

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: feature, experimental  

## netherMaps
Enables normal mapping of the nether

Useful for builds above the nether roof or SkyBlock  
Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: feature  

## optimizedFluidTicks
Optimizes random ticks for fluids

Testing showed around 2-3mspt improvement in regular worlds  
Needs reloading of chunks to be effective  
Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: optimizations, experimental  

## optimizedInventories
Optimizes hoppers and droppers interacting with chests ("Killer Hopper"s)

Tests showed ca. 5-10x performance improvement in worst-case scenarios  
Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: optimizations, experimental  

## optimizedSpawning
Optimizes spawning

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: optimizations, experimental  
Fixes: [MC-151802](https://bugs.mojang.com/browse/MC-151802) fixed in 1.14.3-pre1 (partial)  

## phantomsRespectMobcap
Phantoms don't ignore the mobcap.

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: survival, fix, experimental  

## portalCreativeDelay
Portals won't let a creative player go through instantly

Holding obsidian in either hand won't let you through at all  
Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: creative  

## pushLimit
Piston push limit

Type: `int`  
Default: `12`  
Options: `10`, `12`, `14`, `100`  
Categories: creative  
Validator: `>= 0`  

## railPowerLimit
Rail power limit

Type: `int`  
Default: `9`  
Options: `9`, `15`, `30`  
Categories: creative  
Validator: `> 0`  

## renewableCoral
Coral structures will grow with bonemeal from coral plants

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: feature  

## renewableLava
Obsidian surrounded by 6 lava sources has a chance of converting to lava

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: experimental, feature  

## renewableSand
Cobblestone crushed by falling anvils makes sand

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: feature, experimental  

## shulkerSpawningInEndCities
Shulkers will respawn in end cities

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: feature, experimental  

## silverFishDropGravel
Silverfish drop a gravel item when breaking out of a block

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: feature, experimental  

## sleepingThreshold
carpet.rule.sleepingThreshold.description

Type: `double`  
Default: `100.0`  
Options: `0`, `50`, `100`  
Categories: feature, survival  
Validator: `Range [0.0,100.0]`  

## spawnChunkLevel
Size of the spawn chunks

Like render distance (11 -> 23x23 actively loaded).  
Be aware that a border of 11 chunks will stay loaded around that, once those chunks are loaded somehow.  
Higher levels need lots of RAM (up to 7569 chunks loaded with level 32)  
Type: `int`  
Default: `11`  
Categories: experimental  
Validator: `quickcarpet.settings.Settings$SpawnChunkLevel`  

## stackableShulkerBoxes
Empty shulker boxes can stack to 64 when dropped on the ground

To move them around between inventories, use shift click to move entire stacks  
Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: survival  

## tntHardcodeAngle
Sets the horizontal random angle on TNT for debugging of TNT contraptions

Type: `double`  
Default: `-1.0`  
Options: `-1`  
Categories: tnt  
Validator: `quickcarpet.settings.Settings$TNTAngle`  

## tntPrimeMomentum
Removes random TNT momentum when primed and set to false

Type: `boolean`  
Default: `true`  
Options: `true`, `false`  
Categories: tnt  

