# Quick Carpet Rules
## autoCraftingTable
Automatic crafting table

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: feature, experimental  

## commandCameramode
Enables /c and /s commands to quickly switch between camera and survival modes

/c and /s commands are available to all players regardless of their permission levels  
Type: `boolean`  
Default: `true`  
Options: `true`, `false`  
Categories: commands  

## commandCarpetClone
Enables /carpetclone command

This is an replica of /clone command for fillUpdates and fillLimits  
Type: `boolean`  
Default: `true`  
Options: `true`, `false`  
Categories: commands  

## commandCarpetFill
Enables /carpetfill command

This is an replica of /fill command for fillUpdates and fillLimits  
Type: `boolean`  
Default: `true`  
Options: `true`, `false`  
Categories: commands  

## commandCarpetSetBlock
Enables /player command to control/spawn players

Type: `boolean`  
Default: `true`  
Options: `true`, `false`  
Categories: commands  

## commandLog
Enables /log command to monitor events in the game via chat and overlays

Type: `boolean`  
Default: `true`  
Options: `true`, `false`  
Categories: commands  

## commandPing
Enables /ping for players to get their ping

Type: `boolean`  
Default: `true`  
Options: `true`, `false`  
Categories: commands  

## commandPlayer
Enables /player command to control/spawn players

Type: `boolean`  
Default: `true`  
Options: `true`, `false`  
Categories: commands  

## commandSpawn
Enables /spawn command for spawn tracking

Type: `boolean`  
Default: `true`  
Options: `true`, `false`  
Categories: commands  

## commandTick
Enables /tick command to control game speed

Type: `boolean`  
Default: `true`  
Options: `true`, `false`  
Categories: commands  

## dispensersPlaceBlocks
Dispensers can place most blocks

Type: `Option`  
Default: `false`  
Options: `false`, `whitelist`, `blacklist`, `all`  
Categories: experimental, feature  

## doubleRetraction
1.8 double retraction from pistons.

Gives pistons the ability to double retract without side effects.  
Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: experimental  

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
Validator: `quickcarpet.settings.Validator$Positive`

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
Applies to pistons, observers, droppers, repeaters, stairs, glazed terracotta etc...  
Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: creative, survival  

## hopperCounters
Hoppers pointing to wool will count items passing through them

Enables /counter command, and actions while placing red and green carpets on wool blocks  
Use /counter <color?> reset to reset the counter, and /counter <color?> to query  
In survival, place green carpet on same color wool to query, red to reset the counters  
Counters are global and shared between players, 16 channels available  
Items counted are destroyed, count up to one stack per tick per hopper  
Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: commands  

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

## optimizedSpawning
Optimizes spawning

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: optimizations, experimental  

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
Validator: `quickcarpet.settings.Validator$NonNegative`

## railPowerLimit
Rail power limit

Type: `int`  
Default: `9`  
Options: `9`, `15`, `30`  
Categories: creative  
Validator: `quickcarpet.settings.Validator$Positive`

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

## spawnChunkLevel
Size of spawn chunks

Like render distance (11 -> 23x23 actively loaded).  
Be aware that a border of 11 chunks will stay loaded around that, once those chunks are loaded somehow.  
Higher levels need lots of RAM (up to 7569 chunks loaded with level 32)  
Type: `int`  
Default: `11`  
Options:   
Categories: experimental  
Validator: `quickcarpet.settings.Settings$SpawnChunkLevel`

## stackableShulkerBoxes
Empty shulker boxes can stack to 64 when dropped on the ground

To move them around between inventories, use shift click to move entire stacks  
Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: survival  

