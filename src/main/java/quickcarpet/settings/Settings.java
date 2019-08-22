package quickcarpet.settings;

import net.minecraft.Bootstrap;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Unit;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.dimension.DimensionType;
import quickcarpet.QuickCarpet;
import quickcarpet.annotation.BugFix;
import quickcarpet.feature.PlaceBlockDispenserBehavior;
import quickcarpet.utils.Messenger;
import quickcarpet.utils.Translations;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

import static quickcarpet.settings.RuleCategory.*;

public class Settings {
    public static final CoreSettingsManager MANAGER = new CoreSettingsManager(Settings.class);

    @Rule(category = COMMANDS)
    public static boolean commandTick = true;

    @Rule(category = COMMANDS)
    public static boolean commandPing = true;

    @Rule(category = COMMANDS)
    public static boolean commandCarpetFill = true;

    @Rule(category = COMMANDS)
    public static boolean commandCarpetClone = true;

    @Rule(category = COMMANDS)
    public static boolean commandCarpetSetBlock = true;

    @Rule(category = COMMANDS)
    public static boolean commandPlayer = true;

    @Rule(category = COMMANDS)
    public static boolean commandLog = true;

    @Rule(category = COMMANDS)
    public static boolean commandSpawn = true;

    @Rule(category = COMMANDS)
    public static boolean commandCameramode = true;

    @CreativeDefault("false")
    @Rule(category = CREATIVE)
    public static boolean fillUpdates = true;

    @CreativeDefault("500000")
    @Rule(
            options = {"32768", "250000", "1000000"},
            validator = Validator.Positive.class,
            category = CREATIVE
    )
    public static int fillLimit = 32768;

    @CreativeDefault
    @SurvivalDefault
    @Rule(category = COMMANDS)
    public static boolean hopperCounters = false;

    @Rule(category = TNT)
    public static boolean explosionNoBlockDamage = false;

    @Rule(category = TNT)
    public static boolean tntPrimeMomentum = true;

    @Rule(category = TNT, options = "-1", validator = TNTAngle.class)
    public static double tntHardcodeAngle = -1;

    public static class TNTAngle implements Validator<Double> {
        @Override
        public Optional<TranslatableText> validate(Double value) {
            if (value == -1) return Optional.empty();
            if (value >= 0 && value < 360) return Optional.empty();
            return Optional.of(Messenger.t("carpet.validator.tntAngle"));
        }
    }

    @Rule(category = {FEATURE, EXPERIMENTAL})
    public static boolean silverFishDropGravel = false;

    @Rule(category = {FEATURE, EXPERIMENTAL})
    public static boolean shulkerSpawningInEndCities = false;

    @CreativeDefault
    @Rule(category = CREATIVE)
    public static boolean portalCreativeDelay = false;

    @Rule(category = {FEATURE, EXPERIMENTAL})
    public static boolean fireChargeConvertsToNetherrack = false;

    @Rule(category = {FEATURE, EXPERIMENTAL})
    public static boolean autoCraftingTable = false;

    @Rule(category = {FEATURE, EXPERIMENTAL})
    public static boolean movableBlockEntities = false;

    @Rule(category = SURVIVAL)
    public static boolean stackableShulkerBoxes = false;

    @Rule(category = {OPTIMIZATIONS, EXPERIMENTAL}, bug = @BugFix(value = "MC-151802", fixVersion = "1.14.3-pre1 (partial)"))
    public static boolean optimizedSpawning = false;

    @Rule(category = {FEATURE, EXPERIMENTAL})
    public static boolean mobInFireConvertsSandToSoulsand = false;

    @Rule(category = {FEATURE, EXPERIMENTAL})
    public static boolean renewableSand = false;

    @Rule(category = {EXPERIMENTAL, FEATURE})
    public static PlaceBlockDispenserBehavior.Option dispensersPlaceBlocks = PlaceBlockDispenserBehavior.Option.FALSE;

    @Rule(category = CREATIVE, options = {"10", "12", "14", "100"}, validator = Validator.NonNegative.class)
    public static int pushLimit = 12;

    @Rule(category = CREATIVE, options = {"9", "15", "30"}, validator = Validator.Positive.class)
    public static int railPowerLimit = 9;

    @Rule(category = EXPERIMENTAL, bug = @BugFix("MC-88959"))
    public static boolean doubleRetraction = false;

    @Rule(category = EXPERIMENTAL, onChange = SpawnChunkLevel.class, validator = SpawnChunkLevel.class)
    public static int spawnChunkLevel = 11;

    public static class SpawnChunkLevel implements ChangeListener<Integer>, Validator<Integer> {
        @Override
        public void onChange(ParsedRule<Integer> rule, Integer previous) {
            int newValue = rule.get();
            if (newValue == previous) return;
            ServerWorld overworld = QuickCarpet.minecraft_server.getWorld(DimensionType.OVERWORLD);
            if (overworld != null) {
                ChunkPos centerChunk = new ChunkPos(overworld.getSpawnPos());
                ServerChunkManager chunkManager = (ServerChunkManager) overworld.getChunkManager();
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

    @Rule(category = {EXPERIMENTAL, FEATURE})
    public static boolean renewableLava = false;

    @CreativeDefault
    @SurvivalDefault
    @Rule(category = {CREATIVE, SURVIVAL})
    public static boolean flippinCactus = false;

    @Rule(category = {SURVIVAL, FIX, EXPERIMENTAL})
    public static boolean phantomsRespectMobcap = false;

    @Rule(category = {FIX, EXPERIMENTAL}, bug = @BugFix(value = "MC-152636", fixVersion = "1.14.4-pre1"))
    @BugFixDefault
    public static boolean conversionDupingFix = false;
    
    @Rule(category = FEATURE)
    public static boolean renewableCoral = false;

    @Rule(category = {OPTIMIZATIONS, EXPERIMENTAL})
    public static boolean optimizedFluidTicks = false;

    public static void main(String[] args) throws IOException {
        Bootstrap.initialize();
        Translations.init();
        MANAGER.parse();
        MANAGER.dump(new FileOutputStream(args.length > 0 ? args[0] : "rules.md"));
    }
}
