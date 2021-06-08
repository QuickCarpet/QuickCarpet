package quickcarpet.logging;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.WorldSavePath;
import org.apache.logging.log4j.LogManager;
import quickcarpet.QuickCarpetServer;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class LoggerManager {
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final MinecraftServer server;
    private final Map<String, PlayerSubscriptions> playerSubscriptions = new HashMap<>();
    private final Multimap<Logger, String> subscribedOnlinePlayers = MultimapBuilder.hashKeys().hashSetValues().build();

    public LoggerManager(MinecraftServer server) {
        this.server = server;
    }

    public static final class LoggerOptions {
        public static final MapCodec<LoggerOptions> CODEC = RecordCodecBuilder.mapCodec(it -> it.group(
            Logger.NAME_CODEC.fieldOf("logger").forGetter(o -> o.logger),
            Codec.STRING.optionalFieldOf("option").forGetter(o -> Optional.ofNullable(o.option)),
            LogHandler.CODEC.codec().optionalFieldOf("handler").forGetter(o -> Optional.ofNullable(o.handler))
        ).apply(it, (logger, option, handler) -> LoggerOptions.create(logger, option.orElse(null), handler.orElse(null))));

        public final Logger logger;
        public final String option;
        public final @Nullable LogHandler handler;

        public LoggerOptions(Logger logger, String option, LogHandler handler) {
            this.logger = logger;
            this.option = option;
            this.handler = handler;
        }

        public static LoggerOptions create(Logger logger, @Nullable String option, @Nullable LogHandler handler) {
            if (option == null) option = logger.getDefault();
            if (handler == null) handler = logger.defaultHandler;
            return new LoggerOptions(logger, option, handler);
        }
    }

    public static final class PlayerSubscriptions {
        public static final Codec<Map<String, PlayerSubscriptions>> CODEC = Codec.unboundedMap(Codec.STRING, LoggerOptions.CODEC.codec().listOf().comapFlatMap(
            list -> DataResult.success(new PlayerSubscriptions(list)),
            subs -> new ArrayList<>(subs.subscriptions.values())
        ));

        final Map<Logger, LoggerOptions> subscriptions = new HashMap<>();

        PlayerSubscriptions() {}

        PlayerSubscriptions(Collection<LoggerOptions> subs) {
            for (LoggerOptions options : subs) subscriptions.put(options.logger, options);
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
        subscribePlayer(playerName, LoggerOptions.create(logger, option, handler));
    }

    private void subscribePlayer(String playerName, LoggerOptions options) {
        Logger logger = options.logger;
        PlayerSubscriptions subs = playerSubscriptions.computeIfAbsent(playerName, name -> new PlayerSubscriptions());
        subs.subscriptions.put(logger, options);
        if (playerFromName(playerName) != null) {
            subscribedOnlinePlayers.put(logger, playerName);
            logger.active = true;
        }
        if (options.handler != null) options.handler.onAddPlayer(playerName);
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
        if (handler != null) handler.onRemovePlayer(playerName);
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
        return subs == null ? new PlayerSubscriptions() : subs;
    }

    public Stream<ServerPlayerEntity> getOnlineSubscribers(Logger logger) {
        return subscribedOnlinePlayers.get(logger).stream().map(this::playerFromName);
    }

    public boolean isSubscribed(ServerPlayerEntity player, Logger logger) {
        return subscribedOnlinePlayers.get(logger).contains(player.getEntityName());
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

    private Path getSaveFile() {
        return QuickCarpetServer.getConfigFile(new WorldSavePath("loggerData.json"));
    }

    public void readSaveFile() {
        Path path = getSaveFile();
        if (!Files.isRegularFile(path)) return;
        clear();
        try {
            JsonObject root = JsonHelper.deserialize(Files.newBufferedReader(path, StandardCharsets.UTF_8));
            JsonObject players = root.getAsJsonObject("players");
            readPlayers(players, error -> LOGGER.error("Couldn't read {}: {}", path, error));
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Couldn't read {}", path, e);
        }
    }

    private void readPlayers(JsonObject players, Consumer<String> onError) {
        // TODO: this doesn't give a partial result for each player, but it gives us all players without errors
        PlayerSubscriptions.CODEC.promotePartial(onError).parse(JsonOps.INSTANCE, players).result().ifPresent(map -> {
            playerSubscriptions.putAll(map);
            for (Map.Entry<String, PlayerSubscriptions> e : map.entrySet()) {
                String name = e.getKey();
                ServerPlayerEntity player = playerFromName(name);
                if (player != null) {
                    for (LoggerOptions o : e.getValue().subscriptions.values()) {
                        o.logger.active = true;
                        o.handler.onAddPlayer(name);
                        subscribedOnlinePlayers.put(o.logger, name);
                    }
                }
            }
        });
    }

    public void writeSaveFile() {
        Path path = getSaveFile();
        if (Files.exists(path) && !Files.isRegularFile(path)) {
            LOGGER.error("Couldn't write {}: already exists but is not a regular file", path);
            return;
        }
        try {
            DataResult<JsonElement> result = PlayerSubscriptions.CODEC.encodeStart(JsonOps.INSTANCE, playerSubscriptions);
            JsonElement players = result.getOrThrow(true, error -> LOGGER.warn("Couldn't write {}: {}", path, error));
            JsonObject obj = new JsonObject();
            obj.add("players", players);
            Files.createDirectories(path.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(obj, writer);
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Couldn't write {}", path, e);
        }
    }

    public void clear() {
        for (Map.Entry<String, PlayerSubscriptions> e : playerSubscriptions.entrySet()) {
            for (LoggerOptions o : e.getValue().subscriptions.values()) {
                o.handler.onRemovePlayer(e.getKey());
            }
        }
        playerSubscriptions.clear();
        subscribedOnlinePlayers.clear();
        for (Logger logger : Loggers.values()) logger.active = false;
    }
}
