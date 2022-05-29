package quickcarpet.logging;

import com.mojang.serialization.DataResult;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import quickcarpet.helper.HopperCounter;
import quickcarpet.logging.source.*;
import quickcarpet.settings.Settings;
import quickcarpet.utils.Constants.CarpetCommand;
import quickcarpet.utils.QuickCarpetIdentifier;
import quickcarpet.utils.QuickCarpetRegistries;
import quickcarpet.utils.Translations;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Loggers {
    public static final Logger TNT = register("tnt", builder(LogHandler.CHAT)
        .withOptions("brief", "full")
        .build());
    public static final Logger TPS = register("tps", builder(LogHandler.HUD)
        .withSource(TpsLoggerSource::new)
        .build());
    public static final Logger PACKETS = register("packets", builder(LogHandler.HUD)
        .withSource(PacketCounterLoggerSource::new)
        .build());
    public static final Logger COUNTER = register("counter", builder(LogHandler.HUD)
        .withSource(HopperCounterLoggerSource::new)
        .withOptions(Arrays.stream(HopperCounter.Key.values()).map(k -> k.name).toList())
        .build());
    public static final Logger MOBCAPS = register("mobcaps", builder(LogHandler.HUD)
        .withSource(MobcapsLoggerSource::new)
        .withOptions("dynamic", "overworld", "nether", "end")
        .build());
    public static final Logger LOCAL_MOBCAPS = register("local_mobcaps", builder(LogHandler.HUD)
        .withSource(LocalMobcapsLoggerSource::new)
        .build());
    public static final Logger GC = register("gc", builder(LogHandler.HUD).build());
    public static final Logger COMMAND_BLOCKS = register("command_blocks", builder(LogHandler.CHAT)
        .withOptions("brief", "full")
        .build());
    public static final Logger CAREFUL_BREAK = register("careful_break", builder(null)
        .withUnavailabilityReason(() -> Settings.carefulBreak ? null : new TranslatableText(CarpetCommand.Keys.OPTION_DISABLED, "carefulBreak"))
        .build());
    public static final Logger BLOCK_TICK_LIMIT = register("block_tick_limit", builder(LogHandler.CHAT).build());
    public static final Logger LIGHT_QUEUE = register("light_queue", builder(LogHandler.HUD)
        .withSource(LightQueueLoggerSource::new)
        .build());
    public static final Logger MEMORY = register("memory", builder(LogHandler.HUD)
        .withSource(MemoryUsageLoggerSource::new)
        .build());
    public static final Logger WEATHER = register("weather", builder(LogHandler.CHAT).build());
    public static final Logger KILLS = register("kills", builder(LogHandler.CHAT).build());

//    public static final Logger PROJECTILES<Logger.EmptyCommandParameters> = registerLogger("projectiles", "full",  new String[]{"brief", "full"}, LogHandler.CHAT);
//    public static final Logger FALLING_BLOCKS<Logger.EmptyCommandParameters> = registerLogger("fallingBlocks", "brief", new String[]{"brief", "full"}, LogHandler.CHAT);
//    public static final Logger DAMAGE<Logger.EmptyCommandParameters> = registerLogger("damage", "all", new String[]{"all","players","me"}, LogHandler.CHAT);

    private Loggers() {}

    private static Logger.Builder builder(LogHandler defaultHandler) {
        return new Logger.Builder().withDefaultHandler(defaultHandler);
    }

    private static Logger register(String logName, Logger logger) {
        return register(QuickCarpetIdentifier.of(logName), logger);
    }

    private static Logger register(Identifier id, Logger logger) {
        return Registry.register(QuickCarpetRegistries.LOGGER, id, logger);
    }

    /**
     * Gets the logger with the given name. Returns null if no such logger exists.
     */
    public static Logger getLogger(Identifier name) {
        return getLogger(name, false);
    }

    public static Logger getLogger(Identifier name, boolean includeUnavailable) {
        Logger logger = QuickCarpetRegistries.LOGGER.get(name);
        if (logger == null) return null;
        if (!includeUnavailable && !logger.isAvailable()) return null;
        return logger;
    }

    public static DataResult<Logger> getDataResult(Identifier name) {
        Logger logger = QuickCarpetRegistries.LOGGER.get(name);
        if (logger == null) return DataResult.error("Unknown logger: " + name);
        if (!logger.isAvailable()) {
            return DataResult.error(Translations.translate(logger.getUnavailabilityReason(), Translations.DEFAULT_LOCALE).getString());
        }
        return DataResult.success(logger);
    }

    private static Stream<Map.Entry<RegistryKey<Logger>, Logger>> getAvailableLoggers() {
        return QuickCarpetRegistries.LOGGER.getEntrySet().stream().filter(e -> e.getValue().isAvailable());
    }

    /**
     * Gets the set of logger names.
     */
    public static Set<Identifier> getLoggerNames() {
        return getAvailableLoggers().map(e -> e.getKey().getValue()).collect(Collectors.toSet());
    }

    public static Set<Logger> values() {
        return getAvailableLoggers().map(Map.Entry::getValue).collect(Collectors.toSet());
    }
}
