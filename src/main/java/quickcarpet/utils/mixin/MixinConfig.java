package quickcarpet.utils.mixin;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import quickcarpet.Build;
import quickcarpet.api.settings.ParsedRule;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

public class MixinConfig {
    private static MixinConfig instance;
    private static final Logger LOGGER = LogManager.getLogger("QuickCarpet|MixinConfig");
    private static final Properties DEFAULT_PROPERTIES = new Properties();
    static final String MIXIN_PACKAGE = "quickcarpet.mixin";

    /**
     * Mapping from mixin packages to rules that are disabled together with them
     */
    static final Multimap<String, String> MIXIN_TO_RULES;
    static {
        var identityMapped = Set.of(
            "accurateBlockPlacement",
            "alwaysBaby",
            "antiCheat",
            "autoCraftingTable",
            "autoJukebox",
            "betterChunkLoading",
            "betterStatistics",
            "blockEntityFix",
            "calmNetherFires",
            "carefulBreak",
            "carpets",
            "commandPlayer",
            "commandScoreboardPublic",
            "connectionTimeout",
            "creativeNoClip",
            "doubleRetraction",
            "drownedEnchantedTridentsFix",
            "dustOnPistons",
            "explosionBlockDamage",
            "explosionRange",
            "extremeBehaviors",
            "fallingBlockDuplicationFix",
            "fillLimit",
            "flippinCactus",
            "hopperCounters",
            "hopperMinecartCooldown",
            "hopperMinecartItemTransfer",
            "infiniteHopper",
            "jukeboxRedstoneSignal",
            "lightningKillsDropsFix",
            "localDifficulty",
            "mobcapMultiplier",
            "movableBlockEntities",
            "movableBlockOverrides",
            "nbtMotionLimit",
            "netherMaps",
            "phantomsRespectMobcap",
            "portalCreativeDelay",
            "pushLimit",
            "railPowerLimit",
            "renewableCoral",
            "renewableDeepslate",
            "renewableLava",
            "renewableSoulSand",
            "renewableSponges",
            "sparkingLighter",
            "spawnChunkLevel",
            "spawningAlgorithm",
            "stackableShulkerBoxes",
            "stackableShulkerBoxesInHoppers",
            "stackableShulkerBoxesInInventories",
            "terracottaRepeaters",
            "tileTickLimit",
            "tntUpdateOnPlace",
            "updateSuppressionBlock",
            "updateSuppressionCrashFix",
            "worldBorderSpawningFix",
            "xpCoolDown",
            "xpMerging"
        );
        var builder = ImmutableMultimap.<String, String>builder()
        .putAll("dispenser", "dispensersBreakBlocks", "dispensersPlaceBlocks", "dispensersShearVines", "dispensersStripLogs", "dispensersTillSoil", "renewableNetherrack")
        .putAll("fillUpdates", "fillUpdates", "fillUpdatesPostProcessing")
        .putAll("movingBlockDuplicationFix", "carpetDuplicationFix", "railDuplicationFix", "tntDuplicationFix")
        .putAll("renewableFromAnvil", "renewableSand/anvil", "renewableGravel/anvil", "anvilledIce", "anvilledPackedIce", "anvilledBlueIce")
        .putAll("renewableFromSilverfish", "renewableSand/silverfish", "renewableGravel/silverfish")
        .putAll("structureSpawnOverrides", "huskSpawningInDesertPyramids", "shulkerSpawningInEndCities")
        .putAll("tnt", "tntPrimeMomentum", "tntHardcodeAngle");
        for (String rule : identityMapped) {
            builder.put(rule, rule);
        }
        MIXIN_TO_RULES = builder.build();
    }

    /**
     * Mixins that can be disabled without affecting rules
     */
    static final Set<String> MIXINS_WITHOUT_RULES = Set.of(
        "fabricApi",
        "packetCounter",
        "spawning"
    );

    /**
     * Mixins that can't be disabled
     */
    static final Set<String> CORE_MIXINS = Set.of(
        "accessor",
        "client",
        "core",
        "loggers",
        "profiler",
        "tickSpeed",
        "waypoints"
    );

    private final Set<String> disabledMixins = new HashSet<>();
    private final Set<String> disabledRules = new HashSet<>();
    private final Multimap<String, String> disabledOptions = MultimapBuilder.hashKeys().hashSetValues().build();

    public MixinConfig(Properties properties) {
        Properties props = new Properties(DEFAULT_PROPERTIES);
        props.putAll(properties);
        for (String prop : props.stringPropertyNames()) {
            if (!prop.startsWith("mixin.")) {
                LOGGER.warn("Unknown property " + prop);
                continue;
            }
            String pkg = prop.substring(6);
            Collection<String> rules = MIXIN_TO_RULES.get(pkg);
            if (rules == null && !MIXINS_WITHOUT_RULES.contains(pkg)) {
                if (CORE_MIXINS.contains(pkg)) {
                    LOGGER.warn("Invalid mixin package " + pkg);
                } else {
                    LOGGER.warn("Unknown mixin package " + pkg);
                }
                continue;
            }
            boolean enabled = Boolean.parseBoolean(props.getProperty(prop));
            if (enabled) continue;
            disabledMixins.add(pkg);
            if (rules != null) {
                for (String rule : rules) {
                    int slash = rule.indexOf('/');
                    if (slash >= 0) {
                        String baseRule = rule.substring(0, slash);
                        String option = rule.substring(slash + 1);
                        disabledOptions.put(baseRule, option);
                    } else {
                        disabledRules.add(rule);
                    }
                }
            }
        }
    }

    public boolean isMixinEnabled(String cls) {
        String prefix = MIXIN_PACKAGE + ".";
        if (cls.startsWith(prefix)) cls = cls.substring(prefix.length());
        String[] names = cls.split("\\.");
        return !disabledMixins.contains(names[0]);
    }

    public boolean isRuleEnabled(ParsedRule<?> rule) {
        return !disabledRules.contains(rule.getName());
    }

    public boolean isOptionEnabled(ParsedRule<?> rule, String option) {
        if (!isRuleEnabled(rule)) return false;
        return !disabledOptions.containsEntry(rule.getName(), option);
    }

    public static MixinConfig load(Path path) {
        Properties props = new Properties();
        if (Files.isRegularFile(path)) {
            try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                props.load(reader);
            } catch (IOException e) {
                throw new RuntimeException("Could not load mixin config file", e);
            }
        } else if (Files.notExists(path) && System.getProperty("org.gradle.test.worker") == null) {
            try {
                Files.createDirectories(path.getParent());
                try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                    DEFAULT_PROPERTIES.store(writer, "");
                }
            } catch (IOException e) {
                throw new RuntimeException("Could not write default mixin config file", e);
            }
        }
        return new MixinConfig(props);
    }

    public static MixinConfig getInstance() {
        if (instance == null) {
            instance = load(Paths.get("config", Build.ID + ".properties"));
        }
        return instance;
    }

    static {
        Stream.concat(MIXIN_TO_RULES.keySet().stream(), MIXINS_WITHOUT_RULES.stream())
            .forEach(pkg -> DEFAULT_PROPERTIES.put("mixin." + pkg, "true"));
    }
}
