package quickcarpet.settings;

import net.minecraft.Bootstrap;
import quickcarpet.feature.PlaceBlockDispenserBehavior;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import static quickcarpet.settings.RuleCategory.*;

public class Settings {
    public static final CoreSettingsManager MANAGER = new CoreSettingsManager(Settings.class);

    @Rule(desc = "Enables /tick command to control game speed", category = COMMANDS)
    public static boolean commandTick = true;

    @Rule(desc = "Enables /ping for players to get their ping", category = COMMANDS)
    public static boolean commandPing = true;

    @Rule(
        desc = "Enables /carpetfill command",
        extra = "This is an replica of /fill command for fillUpdates and fillLimits",
        category = COMMANDS
    )
    public static boolean commandCarpetFill = true;

    @Rule(
        desc = "Enables /carpetclone command",
        extra = "This is an replica of /clone command for fillUpdates and fillLimits",
        category = COMMANDS
    )
    public static boolean commandCarpetClone = true;

    @Rule(desc = "Enables /player command to control/spawn players", category = COMMANDS)
    public static boolean commandCarpetSetBlock = true;

    @Rule(desc = "Enables /player command to control/spawn players", category = COMMANDS)
    public static boolean commandPlayer = true;

    @Rule(desc = "Enables /log command to monitor events in the game via chat and overlays", category = COMMANDS)
    public static boolean commandLog = true;

    @Rule(desc = "Enables /spawn command for spawn tracking", category = COMMANDS)
    public static boolean commandSpawn = true;

    @Rule(
        desc = "Enables /c and /s commands to quickly switch between camera and survival modes",
        extra = "/c and /s commands are available to all players regardless of their permission levels",
        category = COMMANDS
    )
    public static boolean commandCameramode = true;

    @CreativeDefault("false")
    @Rule(desc = "fill/clone/setblock and structure blocks cause block updates", category = CREATIVE)
    public static boolean fillUpdates = true;

    @CreativeDefault("500000")
    @Rule(
        desc = "Customizable fill/clone volume limit",
        options = {"32768", "250000", "1000000"},
        validator = Validator.Positive.class,
        category = CREATIVE
    )
    public static int fillLimit = 32768;

    @CreativeDefault
    @SurvivalDefault
    @Rule(
        desc = "Hoppers pointing to wool will count items passing through them",
        extra = {
            "Enables /counter command, and actions while placing red and green carpets on wool blocks",
            "Use /counter <color?> reset to reset the counter, and /counter <color?> to query",
            "In survival, place green carpet on same color wool to query, red to reset the counters",
            "Counters are global and shared between players, 16 channels available",
            "Items counted are destroyed, count up to one stack per tick per hopper"
        },
        category = COMMANDS
    )
    public static boolean hopperCounters = false;

    @Rule(desc = "Explosions won't destroy blocks", category = TNT)
    public static boolean explosionNoBlockDamage = false;

    @Rule(desc = "Silverfish drop a gravel item when breaking out of a block", category = {FEATURE, EXPERIMENTAL})
    public static boolean silverFishDropGravel = false;

    @Rule(desc = "Shulkers will respawn in end cities", category = {FEATURE, EXPERIMENTAL})
    public static boolean shulkerSpawningInEndCities = false;

    @CreativeDefault
    @Rule(
        desc = "Portals won't let a creative player go through instantly",
        extra = "Holding obsidian in either hand won't let you through at all",
        category = CREATIVE
    )
    public static boolean portalCreativeDelay = false;

    @Rule(desc = "Fire charges from dispensers convert cobblestone to netherrack", category = {FEATURE, EXPERIMENTAL})
    public static boolean fireChargeConvertsToNetherrack = false;

    @Rule(desc = "Automatic crafting table", category = {FEATURE, EXPERIMENTAL})
    public static boolean autoCraftingTable = false;
    
    @Rule(desc = "Pistons can push block entities, like hoppers, chests etc.", category = {FEATURE, EXPERIMENTAL})
    public static boolean movableBlockEntities = false;
    
    @Rule(
        desc = "Empty shulker boxes can stack to 64 when dropped on the ground",
        extra = "To move them around between inventories, use shift click to move entire stacks",
        category = SURVIVAL
    )
    public static boolean stackableShulkerBoxes = false;

    @Rule(desc = "Optimizes spawning", category = {OPTIMIZATIONS, EXPERIMENTAL})
    public static boolean optimizedSpawning = false;

    @Rule(desc = "If a living entity dies on sand with fire on top the sand will convert into soul sand", category = {FEATURE, EXPERIMENTAL})
    public static boolean mobInFireConvertsSandToSoulsand = false;
    
    @Rule(desc = "Cobblestone crushed by falling anvils makes sand", category = {FEATURE, EXPERIMENTAL})
    public static boolean renewableSand = false;
    
    @Rule(desc = "Dispensers can place most blocks", category = {EXPERIMENTAL, FEATURE})
    public static PlaceBlockDispenserBehavior.Option dispensersPlaceBlocks = PlaceBlockDispenserBehavior.Option.FALSE;

    @Rule(desc = "Piston push limit", category = CREATIVE, options = {"10", "12", "14", "100"}, validator = Validator.NonNegative.class)
    public static int pushLimit = 12;

    @Rule(desc = "Rail power limit", category = CREATIVE, options = {"9", "15", "30"}, validator = Validator.Positive.class)
    public static int railPowerLimit = 9;

    public static void main(String[] args) throws FileNotFoundException {
        Bootstrap.initialize();
        MANAGER.parse();
        MANAGER.dump(new FileOutputStream(args.length > 0 ? args[0] : "rules.md"));
    }
}
