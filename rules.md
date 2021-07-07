# Quick Carpet Rules
## accurateBlockPlacement
Allows client mods to specify the orientation of placed blocks

Type: `boolean`  
Default: `true`  
Options: `true`, `false`  
Categories: feature  

## alwaysBaby
Allows the player to feed a passive baby mob, a poisonous potato, which makes it a baby.. Forever!

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: feature  

## antiCheat
Prevents players from rubberbanding when moving too fast

Type: `boolean`  
Default: `true`  
Options: `true`, `false`  
Categories: fix  

## anvilledBlueIce
Drop an anvil on packed ice blocks to get blue ice

Value is the number of packed ice blocks required, 0 is disabled  

Type: `int`  
Default: `0`  
Categories: feature, renewable  

## anvilledIce
Drop an anvil on frosted ice blocks to get ice

Value is the number of forsted ice blocks required, 0 is disabled  

Type: `int`  
Default: `0`  
Categories: feature, renewable  

## anvilledPackedIce
Drop an anvil on ice blocks to get packed ice

Value is the number of ice blocks required, 0 is disabled  

Type: `int`  
Default: `0`  
Categories: feature, renewable  

## autoCraftingTable
Automatic crafting table

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: feature  

## betterChunkLoading
Makes things load chunks again

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: feature, experimental  

## betterStatistics
Scoreboards initialize to the statistic values

Type: `boolean`  
Default: `true`  
Options: `true`, `false`  
Categories: feature  

## blockEntityFix
Fixes a crash when activating droppers or dispensers with invalid block entities

Type: `boolean`  
Default: `true`  
Options: `true`, `false`  
Categories: fix  

## calmNetherFires
Make infinite fires calmer to cause less lag

0: disable ticking, 1: vanilla, 2+: multiply the ticking interval  

Type: `int`  
Default: `1`  
Categories: experimental, optimizations  
Validator: `>= 0`  

## cameraModeRestoreLocation
/s restores the player to the location they used /c at

Type: `boolean`  
Default: `true`  
Options: `true`, `false`  
Categories: commands  

## cameraModeNightVision
doing /c will give you night vision and conduit power

Type: `boolean`  
Default: `true`  
Options: `true`, `false`  
Categories: commands

## carefulBreak
Places the mined block in the player inventory when sneaking and subsribed to /log careful_break

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: feature, survival  

## carpetDuplicationFix
Fix carpet duplication using pistons

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: fix  

## commandBlockInfo
Enables /blockinfo command to get info about blocks

Type: `int`  
Default: `0`  
Categories: commands  
Validator: `OP Level (0-4)`  

## commandCameramode
Enables /c, /s and /cs commands to quickly switch between camera and survival modes for players with this permission level

Type: `int`  
Default: `2`  
Categories: commands  
Validator: `OP Level (0-4)`  

## commandDataTracker
Enables /datatracker for viewing tracked entity data

Type: `int`  
Default: `2`  
Categories: commands  
Validator: `OP Level (0-4)`  

## commandFix
Enables /fix command to fix chunk data

Type: `int`  
Default: `2`  
Categories: commands  
Validator: `OP Level (0-4)`  

## commandFluidInfo
Enables /fluidinfo command to get info about fluids

Type: `int`  
Default: `0`  
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

## commandScoreboardPublic
Enables certain /scoreboard commands for all players

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: commands  

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

## connectionTimeout
Set the timeout players trying to connect, 0 to disable timeout

Also fixes timeout to be time-based instead of tick-based, avoiding issues while tick-warping  

Type: `int`  
Default: `30`  
Categories: fix  
Validator: `>= 0`  

## creativeNoClip
Enables players to noclip while in creative mode if they have the client-side setting enabled

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: creative  

## dispensersBreakBlocks
Gives dispensers the ability to break blocks using gunpowder

Type: `Option`  
Default: `false`  
Options: `false`, `normal`, `silk_touch`  
Categories: feature  

## dispensersPlaceBlocks
Dispensers can place most blocks

Type: `Option`  
Default: `false`  
Options: `false`, `whitelist`, `blacklist`, `all`  
Categories: feature  

## dispensersShearVines
Dispensers can shear vines

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: feature  

## dispensersStripLogs
Dispensers with axes can strip logs and wood

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
Categories: fix  
Fixes: [MC-88959](https://bugs.mojang.com/browse/MC-88959)  

## drownedEnchantedTridentsFix
Makes enchantments work on tridents thrown by drowned

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: fix  
Fixes: [MC-127321](https://bugs.mojang.com/browse/MC-127321)  

## dustOnPistons
Makes redstone dust not pop off pistons

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

## fallingBlockDuplicationFix
Fix duplicating falling blocks using end portals

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: fix  

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

## fillUpdatesPostProcessing
Whether fillUpdates=false post-processes the block state (like when upgrading a world)

Type: `boolean`  
Default: `true`  
Options: `true`, `false`  
Categories: creative  

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

## hopperMinecartCooldown
carpet.rule.hopperMinecartCooldown.description

Type: `int`  
Default: `0`  
Options: `0`, `4`, `8`  
Categories: fix  
Validator: `>= 0`  

## hopperMinecartItemTransfer
Hopper minecarts can push out items like normal hoppers

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: feature  

## isDevelopment
Sets the isDevelopment constant

For example enables the /test command  

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: experimental  

## lightningKillsDropsFix
Prevents lightning strikes from destroying the items it creates

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: fix  
Fixes: [MC-206922](https://bugs.mojang.com/browse/MC-206922)  

## movableBlockEntities
Pistons can push block entities, like hoppers, chests etc.

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: feature  

## movableBlockOverrides
Override how pistons interact with any block. Adds weak stickiness behavior used by default for redstone components, flowerpots, etc.

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: feature, experimental  

## nbtMotionLimit
Sets the limit for 'Motion' set via NBT

Set to 0 for no limit (could be dangerous)  

Type: `double`  
Default: `10.0`  
Categories: creative, fix  
Validator: `>= 0`  

## netherMaps
Enables normal mapping of the nether

Useful for builds above the nether roof or SkyBlock  

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: feature  

## phantomsRespectMobcap
Phantoms don't ignore the mobcap.

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: survival, fix  

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

## railDuplicationFix
Fix rail duplication using pistons

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: fix  

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
Categories: feature, renewable  

## renewableGravel
Cobblestone crushed by falling anvils makes gravel, or silverfish drop a gravel item when breaking out of a block

Type: `RenewableGravelOrSandOption`  
Default: `none`  
Options: `none`, `anvil`, `silverfish`  
Categories: feature, renewable  

## renewableLava
Obsidian surrounded by 6 lava sources has a chance of converting to lava

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: feature, renewable  

## renewableNetherrack
Fire charges from dispensers convert cobblestone to netherrack

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: feature, renewable  

## renewableSand
Cobblestone crushed by falling anvils makes sand, or silverfish drop a sand item when breaking out of a block

Type: `RenewableGravelOrSandOption`  
Default: `none`  
Options: `none`, `anvil`, `silverfish`  
Categories: feature, renewable  

## renewableSoulSand
If a living entity dies on sand with fire on top the sand will convert into soul sand

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: feature, renewable  

## renewableSponges
Guardians struck by lightning turn into Elder Guardians

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: feature, renewable  

## shulkerSpawningInEndCities
Shulkers will respawn in end cities

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: feature, renewable  

## smartSaddleDispenser
Keeps the saddle in the dispenser if the entity in front is already saddled

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: feature  

## sparkingLighter
Adds back the ability to create fire in mid-air using Flint and Steel or Fire Charges

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: survival, fix  

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

## terracottaRepeaters
Multiplies the repeater delay by the legacy block data (1-15) of terracotta below or 100 (data value 0: white) instead of the default 2gt

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: feature, creative  

## tileTickLimit
Maximum number of tile-ticks executed per tick

Type: `int`  
Default: `65536`  
Categories: creative  
Validator: `>= 0`  

## tntDuplicationFix
Fix tnt duplication using pistons

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: fix, tnt  

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

## tntUpdateOnPlace
Whether TNT should check for redstone power when placed

Type: `boolean`  
Default: `true`  
Options: `true`, `false`  
Categories: tnt  

## updateSuppressionCrashFix
Fixes updates suppression causing server crashes

Type: `boolean`  
Default: `false`  
Options: `true`, `false`  
Categories: fix, experimental  

## viewDistance
View distance of the dedicated server

Use the vanilla client setting for integrated servers  

Type: `int`  
Default: `-1`  
Validator: `quickcarpet.settings.Settings$ViewDistance`  

## xpCoolDown
Delay before players can absorb the next Experience Orb

Type: `int`  
Default: `2`  
Options: `0`, `2`  
Categories: survival  
Validator: `>= 0`  

## xpMerging
Enables or disables the vanilla xp merging added in 20w45a

Type: `boolean`  
Default: `true`  
Options: `true`, `false`  
Categories: survival  

