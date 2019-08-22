package quickcarpet.logging;

import net.minecraft.util.DyeColor;
import quickcarpet.helper.HopperCounter;
import quickcarpet.helper.Mobcaps;
import quickcarpet.helper.TickSpeed;
import quickcarpet.logging.loghelpers.PacketCounter;
import quickcarpet.logging.loghelpers.TNTLogHelper;
import quickcarpet.utils.CarpetProfiler;

import java.util.*;
import java.util.stream.Collectors;

public final class Loggers {
    // Map from logger names to loggers.
    private static Map<String, Logger<?>> LOGGERS = new HashMap<>();

    public static final Logger<TNTLogHelper.LogParameters> TNT = registerLogger("tnt", "brief", new String[]{"brief", "full"}, LogHandler.CHAT, TNTLogHelper.LogParameters.class);
    public static final Logger<TickSpeed.LogCommandParameters> TPS = registerLogger("tps", null, null, LogHandler.HUD, TickSpeed.LogCommandParameters.class);
    public static final Logger<PacketCounter.LogCommandParameters> PACKETS = registerLogger("packets", null, null, LogHandler.HUD, PacketCounter.LogCommandParameters.class);
    public static final Logger<HopperCounter.LogCommandParameters> COUNTER = registerLogger("counter", "white", Arrays.stream(DyeColor.values()).map(Object::toString).toArray(String[]::new), LogHandler.HUD, HopperCounter.LogCommandParameters.class);
    public static final Logger<Mobcaps.LogCommandParameters> MOBCAPS = registerLogger("mobcaps", "dynamic", new String[]{"dynamic", "overworld", "nether", "end"}, LogHandler.HUD, Mobcaps.LogCommandParameters.class);
    public static final Logger<CarpetProfiler.GCCommandParameters> GC = registerLogger("gc", null, null, LogHandler.CHAT, CarpetProfiler.GCCommandParameters.class);

//    public static final Logger PROJECTILES<Logger.EmptyCommandParameters> = registerLogger("projectiles", "full",  new String[]{"brief", "full"}, LogHandler.CHAT);
//    public static final Logger FALLING_BLOCKS<Logger.EmptyCommandParameters> = registerLogger("fallingBlocks", "brief", new String[]{"brief", "full"}, LogHandler.CHAT);
//    public static final Logger KILLS<Logger.EmptyCommandParameters> = registerLogger("kills", null, null, LogHandler.CHAT);
//    public static final Logger DAMAGE<Logger.EmptyCommandParameters> = registerLogger("damage", "all", new String[]{"all","players","me"}, LogHandler.CHAT);
//    public static final Logger WEATHER<Logger.EmptyCommandParameters> = registerLogger("weather", null, null, LogHandler.CHAT);
//    public static final Logger TILE_TICK_LIMIT<Logger.EmptyCommandParameters> = registerLogger("tileTickLimit", null, null, LogHandler.CHAT);

    private Loggers() {}

    private static Logger<Logger.EmptyCommandParameters> registerLogger(String logName, String def, String[] options, LogHandler defaultHandler) {
        return registerLogger(new Logger<>(logName, def, options, defaultHandler));
    }

    private static <T extends Logger.CommandParameters> Logger<T> registerLogger(String logName, String def, String[] options, LogHandler defaultHandler, Class<T> cls) {
        return registerLogger(new Logger<>(logName, def, options, defaultHandler));
    }

    private static <T extends Logger.CommandParameters> Logger<T> registerLogger(Logger<T> logger) {
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
        if (!includeUnavailable && !logger.isAvailable()) return null;
        return logger;
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

    public static Collection<Logger<?>> values() {
        return LOGGERS.values().stream().filter(Logger::isAvailable).collect(Collectors.toSet());
    }
}
