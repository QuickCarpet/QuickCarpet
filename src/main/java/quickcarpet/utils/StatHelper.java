package quickcarpet.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.UserCache;
import net.minecraft.util.WorldSavePath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import quickcarpet.QuickCarpetServer;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatHelper {
    private static final Logger LOGGER = LogManager.getLogger();
    private static Map<UUID, StatHandler> cache;
    private static long cacheTime;

    public static File[] getStatFiles() {
        try {
            return Files.list(QuickCarpetServer.getConfigFile(new WorldSavePath("stats")))
                    .filter(p -> p.getFileName().toString().endsWith(".json"))
                    .map(Path::toFile)
                    .toArray(File[]::new);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Map<UUID, StatHandler> getAllStatistics(MinecraftServer server) {
        if (cache != null && server.getTicks() - cacheTime < 100) return cache;
        File[] files = getStatFiles();
        HashMap<UUID, StatHandler> stats = new HashMap<>();
        PlayerManager players = server.getPlayerManager();
        for (File file : files) {
            String filename = file.getName();
            String uuidString = filename.substring(0, filename.lastIndexOf(".json"));
            try {
                UUID uuid = UUID.fromString(uuidString);
                ServerPlayerEntity player = players.getPlayer(uuid);
                if (player != null) {
                    stats.put(uuid, player.getStatHandler());
                } else {
                    ServerStatHandler manager = new ServerStatHandler(server, file);
                    stats.put(uuid, manager);
                }
            } catch (IllegalArgumentException ignored) {}
        }
        cache = stats;
        cacheTime = server.getTicks();
        return stats;
    }

    @Nullable
    public static String getUsername(MinecraftServer server, UUID uuid) {
        UserCache profileCache = server.getUserCache();
        GameProfile profile = profileCache.getByUuid(uuid);
        if (profile != null) return profile.getName();
        MinecraftSessionService sessionService = server.getSessionService();
        profile = sessionService.fillProfileProperties(new GameProfile(uuid, null), false);
        if (profile.isComplete()) return profile.getName();
        LOGGER.warn("Could not find name of user " + uuid);
        return null;
    }

    public static void initialize(Scoreboard scoreboard, MinecraftServer server, ScoreboardObjective objective) {
        LOGGER.info("Initializing " + objective);
        ScoreboardCriterion criterion = objective.getCriterion();
        if (!(criterion instanceof Stat)) return;
        Stat<?> stat = ((Stat<?>) criterion);
        Map<UUID, StatHandler> allStats = getAllStatistics(server);
        for (Map.Entry<UUID, StatHandler> statEntry : allStats.entrySet()) {
            StatHandler stats = statEntry.getValue();
            int value = stats.getStat(stat);
            if (value == 0) continue;
            String username = getUsername(server, statEntry.getKey());
            if (username == null) continue;
            ScoreboardPlayerScore score = scoreboard.getPlayerScore(username, objective);
            score.setScore(value);
            LOGGER.info("Initialized score " + objective.getName() + " of " + username + " to " + value);
        }
    }

    public static void clearCache() {
        cache = null;
    }
}
