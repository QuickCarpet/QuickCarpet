package quickcarpet.logging;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.commons.lang3.tuple.Pair;
import quickcarpet.QuickCarpet;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class LoggerManager {
    private Map<String, PlayerSubscriptions> playerSubscriptions = new HashMap<>();
    private Multimap<Logger, String> subscribedOfflinePlayers = MultimapBuilder.hashKeys().hashSetValues().build();
    private Multimap<Logger, String> subscribedOnlinePlayers = MultimapBuilder.hashKeys().hashSetValues().build();

    public static final class PlayerSubscriptions {
        final String playerName;
        final Map<Logger, Pair<LogHandler, String>> subscriptions = new HashMap<>();

        PlayerSubscriptions(String playerName) {
            this.playerName = playerName;
        }

        public boolean isSubscribedTo(Logger logger) {
            return subscriptions.containsKey(logger);
        }

        public String getOption(Logger logger) {
            Pair<LogHandler, String> sub = subscriptions.get(logger);
            return sub == null ? null : sub.getRight();
        }

        public LogHandler getHandler(Logger logger) {
            Pair<LogHandler, String> sub = subscriptions.get(logger);
            return sub == null ? null : sub.getLeft();
        }
    }

    /**
     * Subscribes the player with name playerName to the log with name logName.
     */
    public void subscribePlayer(String playerName, String logName, String option, LogHandler handler) {
        subscribePlayer(playerName, Loggers.getLogger(logName), option, handler);
    }

    private void subscribePlayer(String playerName, Logger logger, String option, LogHandler handler) {
        PlayerSubscriptions subs = playerSubscriptions.computeIfAbsent(playerName, PlayerSubscriptions::new);
        if (option == null) option = logger.getDefault();
        if (handler == null) handler = logger.defaultHandler;
        subs.subscriptions.put(logger, Pair.of(handler, option));
        if (playerFromName(playerName) != null) {
            subscribedOnlinePlayers.put(logger, playerName);
            logger.active = true;
        } else {
            subscribedOfflinePlayers.put(logger, playerName);
        }
        handler.onAddPlayer(playerName);
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
        LogHandler handler = subs.subscriptions.remove(logger).getLeft();
        handler.onRemovePlayer(playerName);
        if (subs.subscriptions.isEmpty()) playerSubscriptions.remove(playerName);
        subscribedOnlinePlayers.remove(logger, playerName);
        subscribedOfflinePlayers.remove(logger, playerName);
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
        return subscribedOnlinePlayers.get(logger).stream().map(LoggerManager::playerFromName);
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
            subscribedOfflinePlayers.remove(logger, playerName);
            logger.active = true;
        }
    }

    public void onPlayerDisconnect(PlayerEntity player) {
        String playerName = player.getEntityName();
        PlayerSubscriptions subs = playerSubscriptions.get(playerName);
        if (subs == null) return;
        for (Logger logger : subs.subscriptions.keySet()) {
            subscribedOfflinePlayers.put(logger, playerName);
            subscribedOnlinePlayers.remove(logger, playerName);
            logger.active = hasOnlineSubscribers(logger);
        }
    }

    /**
     * Gets the {@code PlayerEntity} instance for a player given their UUID. Returns null if they are offline.
     */
    private static ServerPlayerEntity playerFromName(String name) {
        return QuickCarpet.minecraft_server.getPlayerManager().getPlayer(name);
    }
}
