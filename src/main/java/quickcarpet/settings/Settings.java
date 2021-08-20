package quickcarpet.settings;

import net.minecraft.SharedConstants;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Pair;
import net.minecraft.util.Unit;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import quickcarpet.QuickCarpetServer;
import quickcarpet.api.annotation.BugFix;
import quickcarpet.api.settings.*;
import quickcarpet.feature.dispenser.BreakBlockBehavior;
import quickcarpet.feature.dispenser.PlaceBlockBehavior;
import quickcarpet.utils.Messenger;
import quickcarpet.utils.Translations;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

import static quickcarpet.api.settings.RuleCategory.*;

public class Settings {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final RuleUpgrader RULE_UPGRADER = new RuleUpgrader() {
        @Override
        public Pair<String, String> upgrade(String key, String value) {
            return switch (key) {
                case "silverFishDropGravel" -> new Pair<>("renewableGravel", "true".equals(value) ? "silverfish" : "none");
                case "mobInFireConvertsSandToSoulsand" -> new Pair<>("renewableSoulSand", value);
                case "fireChargeConvertsToNetherrack" -> new Pair<>("renewableNetherrack", value);
                default -> null;
            };
        }

        @Override
        public String upgradeValue(ParsedRule<?> rule, String value) {
            if (rule.getCategories().contains(COMMANDS) && rule.getType() == int.class) {
                if ("true".equals(value)) return "0";
                if ("false".equals(value)) return "4";
            }
            switch (rule.getShortName()) {
                case "renewableSand" -> {
                    if ("true".equals(value)) return "anvil";
                    if ("false".equals(value)) return "none";
                }
            }
            return value;
        }
    };
    public static final CoreSettingsManager MANAGER = new quickcarpet.settings.impl.CoreSettingsManager();

    @Rule(category = FEATURE)
    public static boolean accurateBlockPlacement = true;

    @Rule(category = FEATURE)
    public static boolean alwaysBaby = false;

    @Rule(category = FIX)
    public static boolean antiCheat = true;

    @Rule(category = {FEATURE, RENEWABLE})
    public static int anvilledBlueIce = 0;

    @Rule(category = {FEATURE, RENEWABLE})
    public static int anvilledIce = 0;

    @Rule(category = {FEATURE, RENEWABLE})
    public static int anvilledPackedIce = 0;

    @Rule(category = FEATURE)
    public static boolean autoCraftingTable = false;

    @Rule(category = {FEATURE, EXPERIMENTAL})
    public static boolean betterChunkLoading = false;

    @Rule(category = FEATURE)
    public static boolean betterStatistics = true;

    @Rule(category = FIX)
    public static boolean blockEntityFix = true;

    @Rule(category = {EXPERIMENTAL, OPTIMIZATIONS}, validator = Validator.NonNegative.class)
    public static int calmNetherFires = 1;

    @Rule(category = {FEATURE, SURVIVAL})
    public static boolean carefulBreak = false;

    @Rule(category = FIX)
    public static boolean carpetDuplicationFix = false;

    @Rule(category = COMMANDS)
    public static boolean cameraModeRestoreLocation = true;

    @Rule(category = COMMANDS)
    public static boolean cameraModeNightVision = true;

    @Rule(category = COMMANDS, validator = Validator.OpLevel.class)
    public static int commandBlockInfo = 0;

    @Rule(category = COMMANDS, validator = Validator.OpLevel.class)
    public static int commandCameramode = 2;

    @Rule(category = COMMANDS, validator = Validator.OpLevel.class)
    public static int commandDataTracker = 2;

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

    @Rule(category = FIX, validator = Validator.NonNegative.class)
    public static int connectionTimeout = 30;

    @Rule(category = CREATIVE)
    public static boolean creativeNoClip = false;

    @Rule(category = FEATURE)
    public static BreakBlockBehavior.Option dispensersBreakBlocks = BreakBlockBehavior.Option.FALSE;

    @Rule(category = FEATURE)
    public static boolean dispensersInteractCauldron = false;

    @Rule(category = FEATURE)
    public static boolean dispensersPickupBucketables = false;


    @Rule(category = FEATURE)
    public static PlaceBlockBehavior.Option dispensersPlaceBlocks = PlaceBlockBehavior.Option.FALSE;

    @Rule(category = FEATURE)
    public static boolean dispensersShearVines = false;

    @Rule(category = FEATURE)
    public static boolean dispensersStripLogs = false;

    @Rule(category = FEATURE)
    public static boolean dispensersTillSoil = false;

    @Rule(category = FIX, bug = @BugFix("MC-88959"))
    public static boolean doubleRetraction = false;

    @Rule(category = FIX, bug = @BugFix("MC-127321"))
    public static boolean drownedEnchantedTridentsFix;

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

    @Rule(category = FIX)
    public static boolean fallingBlockDuplicationFix = false;

    @Rule(category = CREATIVE)
    public static boolean fillUpdates = true;

    @Rule(category = CREATIVE)
    public static boolean fillUpdatesPostProcessing = true;

    @Rule(category = {CREATIVE, SURVIVAL})
    public static boolean flippinCactus = false;

    @Rule(category = COMMANDS)
    public static boolean hopperCounters = false;

    @Rule(category = FIX, options = {"0", "4", "8"}, validator = Validator.NonNegative.class)
    public static int hopperMinecartCooldown = 0;

    @Rule(category = FEATURE)
    public static boolean hopperMinecartItemTransfer = false;

    @Rule(category = CREATIVE)
    public static boolean infiniteHopper = false;

    @Rule(category = EXPERIMENTAL, onChange = IsDevelopmentListener.class)
    public static boolean isDevelopment = SharedConstants.isDevelopment;

    public static class IsDevelopmentListener implements ChangeListener<Boolean> {
        @Override
        public void onChange(ParsedRule<Boolean> rule, Boolean previous) {
            SharedConstants.isDevelopment = isDevelopment;
            MANAGER.resendCommandTree();
        }
    }

    @Rule(category = FIX, bug = @BugFix("MC-206922"))
    public static boolean lightningKillsDropsFix = false;

    @Rule(category = FEATURE)
    public static boolean movableBlockEntities = false;

    @Rule(category = {FEATURE, EXPERIMENTAL})
    public static boolean movableBlockOverrides = false;

    @Rule(category = {CREATIVE, FIX}, validator = Validator.NonNegative.class)
    public static double nbtMotionLimit = 10;

    @Rule(category = FEATURE)
    public static boolean netherMaps = false;

    @Rule(category = FEATURE)
    public static boolean persistentPlayers = false;

    @Rule(category = {SURVIVAL, FIX})
    public static boolean phantomsRespectMobcap = false;

    @Rule(category = CREATIVE)
    public static boolean portalCreativeDelay = false;

    @Rule(category = CREATIVE, options = {"10", "12", "14", "100"}, validator = Validator.NonNegative.class)
    public static int pushLimit = 12;

    @Rule(category = FIX)
    public static boolean railDuplicationFix = false;

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

    @Rule(category = {FEATURE})
    public static boolean smartSaddleDispenser = false;

    @Rule(category = {FEATURE, RENEWABLE})
    public static boolean shulkerSpawningInEndCities = false;

    @Rule(category = {SURVIVAL, FIX})
    public static boolean sparkingLighter = false;

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

    @Rule(category = EXPERIMENTAL, onChange = SpawnChunkLevel.class, validator = SpawnChunkLevel.class)
    public static int spawnChunkLevel = 11;

    @Rule(category = SURVIVAL)
    public static boolean stackableShulkerBoxes = false;

    @Rule(category = SURVIVAL)
    public static boolean stackableShulkerBoxesInInventories = false;

    @Rule(category = {FEATURE, CREATIVE})
    public static boolean terracottaRepeaters = false;

    @Rule(category = CREATIVE, validator = Validator.NonNegative.class)
    public static int tileTickLimit = 65536;

    @Rule(category = {FIX, TNT})
    public static boolean tntDuplicationFix = false;

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

    @Rule(category = {FIX, EXPERIMENTAL})
    public static boolean updateSuppressionCrashFix = false;

    @Rule(category = {}, validator = ViewDistance.class, onChange = ViewDistance.class)
    public static int viewDistance = -1;

    public static class ViewDistance implements ChangeListener<Integer>, Validator<Integer> {
        @Override
        public Optional<TranslatableText> validate(Integer value) {
            if (value == -1) return Optional.empty();
            MinecraftServer server = QuickCarpetServer.getNullableMinecraftServer();
            if (server != null && !server.isDedicated()) {
                return Optional.of(Messenger.t("carpet.validator.viewDistance.integrated"));
            }
            if (value >= 2 && value <= 32) return Optional.empty();
            return Optional.of(Messenger.t("carpet.validator.range", 2, 32));
        }

        @Override
        public void onChange(ParsedRule<Integer> rule, Integer previous) {
            int newValue = rule.get();
            MinecraftServer server = QuickCarpetServer.getNullableMinecraftServer();
            if (newValue == previous || server == null || !server.isDedicated()) return;
            if (newValue == -1) {
                newValue = ((DedicatedServer) server).getProperties().viewDistance;
            }
            LOGGER.info("Changing view distance to {}, from {}", newValue, server.getPlayerManager().getViewDistance());
            server.getPlayerManager().setViewDistance(newValue);
        }
    }

    @Rule(category = SURVIVAL, options = {"0", "2"}, validator = Validator.NonNegative.class)
    public static int xpCoolDown = 2;

    @Rule(category = SURVIVAL)
    public static boolean xpMerging = true;

    public static void main(String[] args) throws IOException {
        Translations.init();
        MANAGER.parse();
        MANAGER.dump(new FileOutputStream(args.length > 0 ? args[0] : "rules.md"));
    }
}
