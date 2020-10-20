package quickcarpet.settings;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Pair;
import net.minecraft.util.Unit;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import quickcarpet.QuickCarpetServer;
import quickcarpet.annotation.BugFix;
import quickcarpet.feature.BreakBlockDispenserBehavior;
import quickcarpet.feature.PlaceBlockDispenserBehavior;
import quickcarpet.utils.Messenger;
import quickcarpet.utils.Translations;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

import static quickcarpet.settings.RuleCategory.*;

public class Settings {
    public static final CoreSettingsManager MANAGER = new CoreSettingsManager(Settings.class, new RuleUpgrader() {
        @Override
        public Pair<String, String> upgrade(String key, String value) {
            switch (key) {
                case "silverFishDropGravel": return new Pair<>("renewableGravel", "true".equals(value) ? "silverfish" : "none");
                case "mobInFireConvertsSandToSoulsand": return new Pair<>("renewableSoulSand", value);
                case "fireChargeConvertsToNetherrack": return new Pair<>("renewableNetherrack", value);
            }
            return null;
        }

        @Override
        public String upgradeValue(ParsedRule<?> rule, String value) {
            if (rule.categories.contains(RuleCategory.COMMANDS) && rule.type == int.class) {
                if ("true".equals(value)) return "0";
                if ("false".equals(value)) return "4";
            }
            switch (rule.shortName) {
                case "renewableSand": {
                    if ("true".equals(value)) return "anvil";
                    if ("false".equals(value)) return "none";
                }
            }
            return value;
        }
    });

    @Rule(category = FEATURE)
    public static boolean accurateBlockPlacement = true;

    @Rule(category = FIX)
    public static boolean antiCheat = true;

    @Rule(category = {FEATURE, EXPERIMENTAL})
    public static int anvilledBlueIce = 0;

    @Rule(category = {FEATURE, EXPERIMENTAL})
    public static int anvilledIce = 0;

    @Rule(category = {FEATURE, EXPERIMENTAL})
    public static int anvilledPackedIce = 0;

    @Rule(category = {FEATURE, EXPERIMENTAL})
    public static boolean autoCraftingTable = false;

    @Rule(category = {FEATURE, EXPERIMENTAL})
    public static boolean betterChunkLoading = false;

    @Rule(category = {FEATURE, EXPERIMENTAL})
    public static boolean betterStatistics = true;

    @Rule(category = FIX)
    public static boolean blockEntityFix = true;

    @Rule(category = COMMANDS, validator = Validator.OpLevel.class)
    public static int commandBlockInfo = 0;

    @Rule(category = COMMANDS, validator = Validator.OpLevel.class)
    public static int commandCameramode = 2;

    @Rule(category = COMMANDS, validator = Validator.OpLevel.class)
    public static int commandCarpetClone = 2;

    @Rule(category = COMMANDS, validator = Validator.OpLevel.class)
    public static int commandCarpetFill = 2;

    @Rule(category = COMMANDS, validator = Validator.OpLevel.class)
    public static int commandCarpetSetBlock = 2;

    @Rule(category = COMMANDS, validator = Validator.OpLevel.class)
    public static int commandFix = 2;

    @Rule(category = COMMANDS, validator = Validator.OpLevel.class)
    public static int commandFluidInfo = 0;

    @Rule(category = COMMANDS, validator = Validator.OpLevel.class)
    public static int commandLog = 0;

    @Rule(category = COMMANDS, validator = Validator.OpLevel.class)
    public static int commandMeasure = 0;

    @Rule(category = COMMANDS, validator = Validator.OpLevel.class)
    public static int commandPing = 0;

    @Rule(category = COMMANDS, validator = Validator.OpLevel.class)
    public static int commandPlayer = 0;

    @Rule(category = COMMANDS)
    public static boolean commandScoreboardPublic = false;

    @Rule(category = COMMANDS, validator = Validator.OpLevel.class)
    public static int commandSpawn = 0;

    @Rule(category = COMMANDS, validator = Validator.OpLevel.class)
    public static int commandTick = 0;

    @Rule(category = COMMANDS, validator = Validator.OpLevel.class)
    public static int commandTickManipulate = 2;

    @Rule(category = COMMANDS, validator = Validator.OpLevel.class)
    public static int commandWaypoint = 0;

    @Rule(category = {FEATURE, EXPERIMENTAL})
    public static BreakBlockDispenserBehavior.Option dispensersBreakBlocks = BreakBlockDispenserBehavior.Option.FALSE;

    @Rule(category = FEATURE)
    public static PlaceBlockDispenserBehavior.Option dispensersPlaceBlocks = PlaceBlockDispenserBehavior.Option.FALSE;

    @Rule(category = FEATURE)
    public static boolean dispensersShearVines = false;

    @Rule(category = FEATURE)
    public static boolean dispensersStripLogs = false;

    @Rule(category = FEATURE)
    public static boolean dispensersTillSoil = false;

    @Rule(category = EXPERIMENTAL, bug = @BugFix("MC-88959"))
    public static boolean doubleRetraction = false;

    @Rule(category = EXPERIMENTAL)
    public static boolean dustOnPistons = false;

    @Rule(category = TNT)
    public static boolean explosionNoBlockDamage = false;

    @Rule(
            options = {"32768", "250000", "1000000"},
            validator = Validator.Positive.class,
            category = CREATIVE
    )
    public static int fillLimit = 32768;

    @Rule(category = CREATIVE)
    public static boolean fillUpdates = true;

    @Rule(category = CREATIVE)
    public static boolean fillUpdatesPostProcessing = true;

    @Rule(category = {CREATIVE, SURVIVAL})
    public static boolean flippinCactus = false;

    @Rule(category = COMMANDS)
    public static boolean hopperCounters = false;

    @Rule(category = EXPERIMENTAL, onChange = IsDevelopmentListener.class)
    public static boolean isDevelopment = false;

    public static class IsDevelopmentListener implements ChangeListener<Boolean> {
        @Override
        public void onChange(ParsedRule<Boolean> rule, Boolean previous) {
            SharedConstants.isDevelopment = isDevelopment;
            MANAGER.resendCommandTree();
        }
    }

    @Rule(category = {FEATURE, EXPERIMENTAL})
    public static boolean movableBlockEntities = false;

    @Rule(category = {FEATURE,EXPERIMENTAL})
    public static boolean movableBlockOverrides = false;

    @Rule(category = FEATURE)
    public static boolean netherMaps = false;

    @Rule(category = {OPTIMIZATIONS, EXPERIMENTAL})
    public static boolean optimizedFluidTicks = false;

    @Rule(category = {OPTIMIZATIONS, EXPERIMENTAL}, deprecated = true)
    public static boolean optimizedInventories = false;

    @Rule(category = {OPTIMIZATIONS, EXPERIMENTAL}, bug = @BugFix(value = "MC-151802", fixVersion = "1.14.3-pre1 (partial)"))
    public static boolean optimizedSpawning = false;

    @Rule(category = {SURVIVAL, FIX, EXPERIMENTAL})
    public static boolean phantomsRespectMobcap = false;

    @Rule(category = CREATIVE)
    public static boolean portalCreativeDelay = false;

    @Rule(category = CREATIVE, options = {"10", "12", "14", "100"}, validator = Validator.NonNegative.class)
    public static int pushLimit = 12;

    @Rule(category = CREATIVE, options = {"9", "15", "30"}, validator = Validator.Positive.class)
    public static int railPowerLimit = 9;

    @Rule(category = {FEATURE, RENEWABLE})
    public static boolean renewableCoral = false;

    public enum RenewableGravelOrSandOption {
        NONE, ANVIL, SILVERFISH
    }

    @Rule(category = {FEATURE, RENEWABLE})
    public static RenewableGravelOrSandOption renewableGravel = RenewableGravelOrSandOption.NONE;

    @Rule(category = {FEATURE, RENEWABLE})
    public static boolean renewableLava = false;

    @Rule(category = {FEATURE, RENEWABLE})
    public static boolean renewableNetherrack = false;

    @Rule(category = {FEATURE, RENEWABLE})
    public static RenewableGravelOrSandOption renewableSand = RenewableGravelOrSandOption.NONE;

    @Rule(category = {FEATURE, RENEWABLE})
    public static boolean renewableSoulSand = false;

    @Rule(category = {FEATURE, RENEWABLE})
    public static boolean renewableSponges = false;

    @Rule(category = {FEATURE, RENEWABLE})
    public static boolean shulkerSpawningInEndCities = false;

    public static class SleepingThreshold extends Validator.Range<Double> {
        public SleepingThreshold() {
            super(0.0, 100.0);
        }
    }

    @Rule(category = {FEATURE, SURVIVAL}, options = {"0", "50", "100"}, validator = SleepingThreshold.class)
    public static double sleepingThreshold = 100;

    public static class SpawnChunkLevel implements ChangeListener<Integer>, Validator<Integer> {
        @Override
        public void onChange(ParsedRule<Integer> rule, Integer previous) {
            int newValue = rule.get();
            MinecraftServer server = QuickCarpetServer.getNullableMinecraftServer();
            if (newValue == previous || server == null) return;
            ServerWorld overworld = server.getWorld(World.OVERWORLD);
            if (overworld != null) {
                ChunkPos centerChunk = new ChunkPos(overworld.getSpawnPos());
                ServerChunkManager chunkManager = overworld.getChunkManager();
                chunkManager.removeTicket(ChunkTicketType.START, centerChunk, previous, Unit.INSTANCE);
                chunkManager.addTicket(ChunkTicketType.START, centerChunk, newValue, Unit.INSTANCE);
            }
        }

        @Override
        public Optional<TranslatableText> validate(Integer value) {
            if (value < 1 || value > 32) return Optional.of(Messenger.t("carpet.validator.range", 1, 32));
            return Optional.empty();
        }
    }

    @Rule(category = {SURVIVAL, FIX})
    public static boolean sparkingLighter = false;

    @Rule(category = EXPERIMENTAL, onChange = SpawnChunkLevel.class, validator = SpawnChunkLevel.class)
    public static int spawnChunkLevel = 11;

    @Rule(category = SURVIVAL)
    public static boolean stackableShulkerBoxes = false;

    @Rule(category = {FEATURE, CREATIVE})
    public static boolean terracottaRepeaters = false;

    public static class TNTAngle implements Validator<Double> {
        @Override
        public Optional<TranslatableText> validate(Double value) {
            if (value == -1) return Optional.empty();
            if (value >= 0 && value < 360) return Optional.empty();
            return Optional.of(Messenger.t("carpet.validator.tntAngle"));
        }
    }

    @Rule(category = TNT, options = "-1", validator = TNTAngle.class)
    public static double tntHardcodeAngle = -1;

    @Rule(category = TNT)
    public static boolean tntPrimeMomentum = true;

    @Rule(category = TNT)
    public static boolean tntUpdateOnPlace = true;

    @Rule(category = {SURVIVAL, EXPERIMENTAL}, options = {"0", "2"}, validator = Validator.NonNegative.class)
    public static int xpCoolDown = 2;

    @Rule(category = {SURVIVAL, EXPERIMENTAL})
    public static boolean xpMerging = false;

    public static void main(String[] args) throws IOException {
        Bootstrap.initialize();
        Translations.init();
        MANAGER.parse();
        MANAGER.dump(new FileOutputStream(args.length > 0 ? args[0] : "rules.md"));
    }
}
