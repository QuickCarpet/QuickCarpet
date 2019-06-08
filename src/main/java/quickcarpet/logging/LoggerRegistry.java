package quickcarpet.logging;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DyeColor;
import quickcarpet.helper.HopperCounter;
import quickcarpet.helper.TickSpeed;
import quickcarpet.logging.loghelpers.PacketCounter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LoggerRegistry {
    // Map from logger names to loggers.
    private static Map<String, Logger> loggerRegistry = new HashMap<>();
    // Map from player names to the set of names of the logs that player is subscribed to.
    private static Map<String, Map<String, String>> playerSubscriptions = new HashMap<>();

    public static final Logger<Logger.EmptyCommandParameters> TNT = registerLogger("tnt", "brief", new String[]{"brief", "full"}, LogHandler.CHAT);
    public static final Logger<TickSpeed.LogCommandParameters> TPS = registerLogger("tps", null, null, LogHandler.HUD, TickSpeed.LogCommandParameters.class);
    public static final Logger<PacketCounter.LogCommandParameters> PACKETS = registerLogger("packets", null, null, LogHandler.HUD, PacketCounter.LogCommandParameters.class);
    public static final Logger<HopperCounter.LogCommandParameters> COUNTER = registerLogger("counter", "white", Arrays.stream(DyeColor.values()).map(Object::toString).toArray(String[]::new), LogHandler.HUD, HopperCounter.LogCommandParameters.class);
    public static final Logger<Logger.EmptyCommandParameters> MOBCAPS = registerLogger("mobcaps", "dynamic", new String[]{"dynamic", "overworld", "nether", "end"}, LogHandler.HUD);

//    public static final Logger PROJECTILES<Logger.EmptyCommandParameters> = registerLogger("projectiles", "full",  new String[]{"brief", "full"}, LogHandler.CHAT);
//    public static final Logger FAILLING_BLOCKS<Logger.EmptyCommandParameters> = registerLogger("fallingBlocks", "brief", new String[]{"brief", "full"}, LogHandler.CHAT);
//    public static final Logger KILLS<Logger.EmptyCommandParameters> = registerLogger("kills", null, null, LogHandler.CHAT);
//    public static final Logger DAMAGE<Logger.EmptyCommandParameters> = registerLogger("damage", "all", new String[]{"all","players","me"}, LogHandler.CHAT);
//    public static final Logger WEATHER<Logger.EmptyCommandParameters> = registerLogger("weather", null, null, LogHandler.CHAT);
//    public static final Logger TILE_TICK_LIMIT<Logger.EmptyCommandParameters> = registerLogger("tileTickLimit", null, null, LogHandler.CHAT);

    /**
     * Gets the logger with the given name. Returns null if no such logger exists.
     */
    public static Logger getLogger(String name) {
        return loggerRegistry.get(name);
    }

    /**
     * Gets the set of logger names.
     */
    public static Set<String> getLoggerNames() {
        return loggerRegistry.keySet();
    }

    /**
     * Subscribes the player with name playerName to the log with name logName.
     */
    public static void subscribePlayer(String playerName, String logName, String option, LogHandler handler) {
        if (!playerSubscriptions.containsKey(playerName)) playerSubscriptions.put(playerName, new HashMap<>());
        Logger log = loggerRegistry.get(logName);
        if (option == null) option = log.getDefault();
        playerSubscriptions.get(playerName).put(logName, option);
        log.addPlayer(playerName, option, handler);
    }

    /**
     * Unsubscribes the player with name playerName from the log with name logName.
     */
    public static void unsubscribePlayer(String playerName, String logName) {
        if (playerSubscriptions.containsKey(playerName)) {
            Map<String, String> subscriptions = playerSubscriptions.get(playerName);
            subscriptions.remove(logName);
            loggerRegistry.get(logName).removePlayer(playerName);
            if (subscriptions.size() == 0) playerSubscriptions.remove(playerName);
        }
    }

    /**
     * If the player is not subscribed to the log, then subscribe them. Otherwise, unsubscribe them.
     */
    public static boolean togglePlayerSubscription(String playerName, String logName, LogHandler handler) {
        if (playerSubscriptions.containsKey(playerName) && playerSubscriptions.get(playerName).containsKey(logName)) {
            unsubscribePlayer(playerName, logName);
            return false;
        } else {
            subscribePlayer(playerName, logName, null, handler);
            return true;
        }
    }

    /**
     * Get the set of logs the current player is subscribed to.
     */
    public static Map<String, String> getPlayerSubscriptions(String playerName) {
        if (playerSubscriptions.containsKey(playerName)) {
            return playerSubscriptions.get(playerName);
        }
        return null;
    }

    private static Logger<Logger.EmptyCommandParameters> registerLogger(String logName, String def, String[] options, LogHandler defaultHandler) {
        return registerLogger(new Logger<>(logName, def, options, defaultHandler));
    }

    private static <T extends Logger.CommandParameters> Logger<T> registerLogger(String logName, String def, String[] options, LogHandler defaultHandler, Class<T> cls) {
        return registerLogger(new Logger<>(logName, def, options, defaultHandler));
    }

    private static <T extends Logger.CommandParameters> Logger<T> registerLogger(Logger<T> logger) {
        loggerRegistry.put(logger.getLogName(), logger);
        return logger;
    }

    // TODO: hook up
    public static void playerConnected(PlayerEntity player) {
        for (Logger log : loggerRegistry.values()) {
            log.onPlayerConnect(player);
        }
    }

    // TODO: hook up
    public static void playerDisconnected(PlayerEntity player) {
        for (Logger log : loggerRegistry.values()) {
            log.onPlayerDisconnect(player);
        }
    }
}
