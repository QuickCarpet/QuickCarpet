package quickcarpet.logging;

import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import quickcarpet.QuickCarpet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Logger<T extends Logger.CommandParameters> {
    private boolean active = false;
    // The set of subscribed and online players.
    private Map<String, String> subscribedOnlinePlayers;

    // The set of subscribed and offline players.
    private Map<String, String> subscribedOfflinePlayers;

    // The logName of this log. Gets prepended to logged messages.
    private String logName;

    private String default_option;

    private String[] options;

    private LogHandler defaultHandler;

    // The map of player names to the log handler used
    private Map<String, LogHandler> handlers;

    public Logger(String logName, String def, String[] options, LogHandler defaultHandler) {
        subscribedOnlinePlayers = new HashMap<>();
        subscribedOfflinePlayers = new HashMap<>();
        this.logName = logName;
        this.default_option = def;
        this.options = options == null ? new String[0] : options;
        this.defaultHandler = defaultHandler;
        handlers = new HashMap<>();
    }

    public String getDefault() {
        return default_option;
    }

    public String[] getOptions() {
        return options;
    }

    public String getLogName() {
        return logName;
    }

    /**
     * Subscribes the player with the given logName to the logger.
     */
    public void addPlayer(String playerName, String option, LogHandler handler) {
        if (playerFromName(playerName) != null) {
            subscribedOnlinePlayers.put(playerName, option);
            active = true;
        } else {
            subscribedOfflinePlayers.put(playerName, option);
        }
        if (handler == null)
            handler = defaultHandler;
        handlers.put(playerName, handler);
        handler.onAddPlayer(playerName);
    }

    /**
     * Unsubscribes the player with the given logName from the logger.
     */
    public void removePlayer(String playerName) {
        handlers.getOrDefault(playerName, defaultHandler).onRemovePlayer(playerName);
        subscribedOnlinePlayers.remove(playerName);
        subscribedOfflinePlayers.remove(playerName);
        handlers.remove(playerName);
        active = hasOnlineSubscribers();
    }

    /**
     * Sets the LogHandler for the given player
     */
    public void setHandler(String playerName, LogHandler newHandler) {
        if (newHandler == null)
            newHandler = defaultHandler;
        LogHandler oldHandler = handlers.getOrDefault(playerName, defaultHandler);
        if (oldHandler != newHandler) {
            oldHandler.onRemovePlayer(playerName);
            handlers.put(playerName, newHandler);
            newHandler.onAddPlayer(playerName);
        }
    }

    /**
     * Returns true if there are any online subscribers for this log.
     */
    public boolean hasOnlineSubscribers() {
        return subscribedOnlinePlayers.size() > 0;
    }

    public boolean isActive() {
        return active;
    }

    /**
     * serves messages to players fetching them from the promise
     * will repeat invocation for players that share the same option
     */
    @FunctionalInterface
    public interface MessageSupplier {
        Text[] get(String playerOption, PlayerEntity player);
    }

    public void log(MessageSupplier message) {
        this.log(message, (Supplier<T>) EmptyCommandParameters.SUPPLIER);
    }

    public void log(MessageSupplier message, Supplier<T> commandParams) {
        for (Map.Entry<String, String> en : subscribedOnlinePlayers.entrySet()) {
            ServerPlayerEntity player = playerFromName(en.getKey());
            if (player != null) {
                Text[] messages = message.get(en.getValue(), player);
                if (messages != null)
                    sendPlayerMessage(en.getKey(), player, messages, commandParams);
            }
        }
    }

    /**
     * guarantees that each message for each option will be evaluated once from the promise
     * and served the same way to all other players subscribed to the same option
     */
    @FunctionalInterface
    public interface PlayerIndependentMessageSupplier extends MessageSupplier {
        Text[] get(String playerOption);
        default Text[] get(String playerOption, PlayerEntity player) {
            return get(playerOption);
        }
    }

    public void log(PlayerIndependentMessageSupplier message) {
        this.log(message, (Supplier<T>) EmptyCommandParameters.SUPPLIER);
    }

    public void log(PlayerIndependentMessageSupplier message, Supplier<T>  commandParams) {
        Map<String, Text[]> cannedMessages = new HashMap<>();
        for (Map.Entry<String, String> en : subscribedOnlinePlayers.entrySet()) {
            ServerPlayerEntity player = playerFromName(en.getKey());
            if (player != null) {
                String option = en.getValue();
                if (!cannedMessages.containsKey(option)) {
                    cannedMessages.put(option, message.get(option));
                }
                Text[] messages = cannedMessages.get(option);
                if (messages != null)
                    sendPlayerMessage(en.getKey(), player, messages, commandParams);
            }
        }
    }

    public void log(Supplier<Text[]> message) {
        this.log(message, (Supplier<T>) EmptyCommandParameters.SUPPLIER);
    }


    public void log(Supplier<Text[]> message, Supplier<T>  commandParams) {
        Text[] cannedMessages = null;
        for (Map.Entry<String, String> en : subscribedOnlinePlayers.entrySet()) {
            ServerPlayerEntity player = playerFromName(en.getKey());
            if (player != null) {
                if (cannedMessages == null) cannedMessages = message.get();
                sendPlayerMessage(en.getKey(), player, cannedMessages, commandParams);
            }
        }
    }

    public void sendPlayerMessage(String playerName, ServerPlayerEntity player, Text[] messages, Supplier<T> commandParams) {
        handlers.getOrDefault(playerName, defaultHandler).handle(player, messages, (Supplier<CommandParameters>) commandParams);
    }

    /**
     * Gets the {@code PlayerEntity} instance for a player given their UUID. Returns null if they are offline.
     */
    protected ServerPlayerEntity playerFromName(String name) {
        return QuickCarpet.minecraft_server.getPlayerManager().getPlayer(name);
    }

    // ----- Event Handlers ----- //

    public void onPlayerConnect(PlayerEntity player) {
        // If the player was subscribed to the log and offline, move them to the set of online subscribers.
        String playerName = player.getEntityName();
        if (subscribedOfflinePlayers.containsKey(playerName)) {
            subscribedOnlinePlayers.put(playerName, subscribedOfflinePlayers.get(playerName));
            subscribedOfflinePlayers.remove(playerName);
            active = true;
        }
    }

    public void onPlayerDisconnect(PlayerEntity player) {
        // If the player was subscribed to the log, move them to the set of offline subscribers.
        String playerName = player.getEntityName();
        if (subscribedOnlinePlayers.containsKey(playerName)) {
            subscribedOfflinePlayers.put(playerName, subscribedOnlinePlayers.get(playerName));
            subscribedOnlinePlayers.remove(playerName);
            active = hasOnlineSubscribers();
        }
    }

    public String getAcceptedOption(String arg) {
        if (options != null && Arrays.asList(options).contains(arg)) return arg;
        return null;
    }

    public interface CommandParameters<T> extends Map<String, T> {}

    public static class EmptyCommandParameters extends Object2ObjectMaps.EmptyMap<String, Object> implements CommandParameters<Object> {
        public static final EmptyCommandParameters INSTANCE = new EmptyCommandParameters();
        public static final Supplier<EmptyCommandParameters> SUPPLIER = () -> INSTANCE;
        private EmptyCommandParameters() {}
    }
}
