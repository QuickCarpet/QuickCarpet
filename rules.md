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

## stackableShulkerBoxes
Empty shulker boxes can stack to 64 when dropped on the ground

To move them around between inventories, use shift click to move entire stacks  
Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: survival  

