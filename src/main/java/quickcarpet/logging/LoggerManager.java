package quickcarpet.logging;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class LoggerManager {
    private final MinecraftServer server;
    private final Map<String, PlayerSubscriptions> playerSubscriptions = new HashMap<>();
    private final Multimap<Logger, String> subscribedOnlinePlayers = MultimapBuilder.hashKeys().hashSetValues().build();

    public LoggerManager(MinecraftServer server) {
        this.server = server;
    }

    public static final class LoggerOptions {
        public final String option;
        public final LogHandler handler;

        public LoggerOptions(String option, LogHandler handler) {
            this.option = option;
            this.handler = handler;
        }
    }

    public static final class PlayerSubscriptions {
        final String playerName;
        final Map<Logger, LoggerOptions> subscriptions = new HashMap<>();

        PlayerSubscriptions(String playerName) {
            this.playerName = playerName;
        }

        public boolean isSubscribedTo(Logger logger) {
            return subscriptions.containsKey(logger);
        }

        public String getOption(Logger logger) {
            LoggerOptions sub = subscriptions.get(logger);
            return sub == null ? null : sub.option;
        }

        public LogHandler getHandler(Logger logger) {
            LoggerOptions sub = subscriptions.get(logger);
            return sub == null ? null : sub.handler;
        }
    }

    /**
     * Subscribes the player with name playerName to the log with name logName.
     */
    public void subscribePlayer(String playerName, String logName, String option, LogHandler handler) {
        subscribePlayer(playerName, Loggers.getLogger(logName), option, handler);
    }

    private void subscribePlayer(String playerName, Logger logger, String option, LogHandler handler) {
        if (option == null) option = logger.getDefault();
        if (handler == null) handler = logger.defaultHandler;
        subscribePlayer(playerName, logger, new LoggerOptions(option, handler));
    }

    private void subscribePlayer(String playerName, Logger logger, LoggerOptions options) {
        PlayerSubscriptions subs = playerSubscriptions.computeIfAbsent(playerName, PlayerSubscriptions::new);
        subs.subscriptions.put(logger, options);
        if (playerFromName(playerName) != null) {
            subscribedOnlinePlayers.put(logger, playerName);
            logger.active = true;
        }
        options.handler.onAddPlayer(playerName);
    }

    /**
     * Unsubscribes the player with name playerName from the log with name logName.
     */
    public void unsubscribePlayer(String playerName, String logName) {
        unsubscribePlayer(playerName, Loggers.getLogger(logName));
    }

    private void unsubscribePlayer(String playerName, Logger logger) {
        PlayerSubscriptions subs = playerSubscriptions.get(playerName);
        if (subs == null) return;
        LogHandler handler = subs.subscriptions.remove(logger).handler;
        handler.onRemovePlayer(playerName);
        if (subs.subscriptions.isEmpty()) playerSubscriptions.remove(playerName);
        subscribedOnlinePlayers.remove(logger, playerName);
        logger.active = hasOnlineSubscribers(logger);
    }

    /**
     * If the player is not subscribed to the log, then subscribe them. Otherwise, unsubscribe them.
     */
    public boolean togglePlayerSubscription(String playerName, String logName, LogHandler handler) {
        PlayerSubscriptions subs = playerSubscriptions.get(playerName);
        Logger logger = Loggers.getLogger(logName);
        if (subs != null && subs.isSubscribedTo(logger)) {
            unsubscribePlayer(playerName, logger);
            return false;
        } else {
            subscribePlayer(playerName, logger, null, handler);
            return true;
        }
    }

    /**
     * Get the set of logs the current player is subscribed to.
     */
    public PlayerSubscriptions getPlayerSubscriptions(String playerName) {
        PlayerSubscriptions subs = playerSubscriptions.get(playerName);
        return subs == null ? new PlayerSubscriptions(playerName) : subs;
    }

    public Stream<ServerPlayerEntity> getOnlineSubscribers(Logger logger) {
        return subscribedOnlinePlayers.get(logger).stream().map(this::playerFromName);
    }

    public boolean hasOnlineSubscribers(Logger logger) {
        return !subscribedOnlinePlayers.get(logger).isEmpty();
    }

    public void onPlayerConnect(PlayerEntity player) {
        String playerName = player.getEntityName();
        PlayerSubscriptions subs = playerSubscriptions.get(playerName);
        if (subs == null) return;
        for (Logger logger : subs.subscriptions.keySet()) {
            subscribedOnlinePlayers.put(logger, playerName);
            logger.active = true;
        }
    }

    public void onPlayerDisconnect(PlayerEntity player) {
        String playerName = player.getEntityName();
        PlayerSubscriptions subs = playerSubscriptions.get(playerName);
        if (subs == null) return;
        for (Logger logger : subs.subscriptions.keySet()) {
            subscribedOnlinePlayers.remove(logger, playerName);
            logger.active = hasOnlineSubscribers(logger);
        }
    }

    /**
     * Gets the {@code PlayerEntity} instance for a player given their UUID. Returns null if they are offline.
     */
    private ServerPlayerEntity playerFromName(String name) {
        return server.getPlayerManager().getPlayer(name);
    }
}
