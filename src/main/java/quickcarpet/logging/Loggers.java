package quickcarpet.logging;

import com.mojang.serialization.DataResult;
import net.minecraft.util.DyeColor;
import quickcarpet.utils.Translations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class Loggers {
    // Map from logger names to loggers.
    private static final Map<String, Logger> LOGGERS = new HashMap<>();

    public static final Logger TNT = registerLogger("tnt", "brief", new String[]{"brief", "full"}, LogHandler.CHAT);
    public static final Logger TPS = registerLogger("tps", null, null, LogHandler.HUD);
    public static final Logger PACKETS = registerLogger("packets", null, null, LogHandler.HUD);
    public static final Logger COUNTER = registerLogger("counter", "white", Arrays.stream(DyeColor.values()).map(Object::toString).toArray(String[]::new), LogHandler.HUD);
    public static final Logger MOBCAPS = registerLogger("mobcaps", "dynamic", new String[]{"dynamic", "overworld", "nether", "end"}, LogHandler.HUD);
    public static final Logger GC = registerLogger("gc", null, null, LogHandler.CHAT);

//    public static final Logger PROJECTILES<Logger.EmptyCommandParameters> = registerLogger("projectiles", "full",  new String[]{"brief", "full"}, LogHandler.CHAT);
//    public static final Logger FALLING_BLOCKS<Logger.EmptyCommandParameters> = registerLogger("fallingBlocks", "brief", new String[]{"brief", "full"}, LogHandler.CHAT);
//    public static final Logger KILLS<Logger.EmptyCommandParameters> = registerLogger("kills", null, null, LogHandler.CHAT);
//    public static final Logger DAMAGE<Logger.EmptyCommandParameters> = registerLogger("damage", "all", new String[]{"all","players","me"}, LogHandler.CHAT);
//    public static final Logger WEATHER<Logger.EmptyCommandParameters> = registerLogger("weather", null, null, LogHandler.CHAT);
//    public static final Logger TILE_TICK_LIMIT<Logger.EmptyCommandParameters> = registerLogger("tileTickLimit", null, null, LogHandler.CHAT);

    private Loggers() {}

    private static Logger registerLogger(String logName, String def, String[] options, LogHandler defaultHandler) {
        return registerLogger(new Logger(logName, def, options, defaultHandler));
    }

    private static Logger registerLogger(Logger logger) {
        LOGGERS.put(logger.getName(), logger);
        return logger;
    }

    /**
     * Gets the logger with the given name. Returns null if no such logger exists.
     */
    public static Logger getLogger(String name) {
        return getLogger(name, false);
    }

    public static Logger getLogger(String name, boolean includeUnavailable) {
        Logger logger = LOGGERS.get(name);
        if (logger == null) return null;
        if (!includeUnavailable && !logger.isAvailable()) return null;
        return logger;
    }

    public static DataResult<Logger> getDataResult(String name) {
        Logger logger = LOGGERS.get(name);
        if (logger == null) return DataResult.error("Unknown logger: " + name);
        if (!logger.isAvailable()) {
            return DataResult.error(Translations.translate(logger.getUnavailabilityReason(), Translations.DEFAULT_LOCALE).getString());
        }
        return DataResult.success(logger);
    }

    /**
     * Gets the set of logger names.
     */
    public static Set<String> getLoggerNames() {
        return LOGGERS.entrySet().stream()
                .filter(e -> e.getValue().isAvailable())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public static Set<Logger> values() {
        return LOGGERS.values().stream().filter(Logger::isAvailable).collect(Collectors.toSet());
    }
}
