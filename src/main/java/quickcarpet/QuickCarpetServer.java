package quickcarpet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.level.ServerWorldProperties;
import quickcarpet.api.QuickCarpetServerAPI;
import quickcarpet.api.ServerEventListener;
import quickcarpet.api.TelemetryProvider;
import quickcarpet.api.network.server.ServerPluginChannelManager;
import quickcarpet.helper.Mobcaps;
import quickcarpet.helper.TickSpeed;
import quickcarpet.logging.LoggerManager;
import quickcarpet.network.channels.RulesChannel;
import quickcarpet.network.channels.StructureChannel;
import quickcarpet.network.impl.PluginChannelManager;
import quickcarpet.pubsub.PubSubMessenger;
import quickcarpet.utils.HUDController;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Map;

import static quickcarpet.api.network.server.ServerPluginChannelManager.LOG;

public class QuickCarpetServer implements QuickCarpetServerAPI, ServerEventListener, TelemetryProvider {
    private static QuickCarpetServer instance;
    public final MinecraftServer server;
    public ServerPluginChannelManager pluginChannels;
    private final PubSubMessenger pubSubMessenger = new PubSubMessenger(QuickCarpet.PUBSUB);
    public LoggerManager loggers;
    public TickSpeed tickSpeed;

    private QuickCarpetServer(MinecraftServer server) {
        this.server = server;
        pluginChannels = new PluginChannelManager(server);
        pluginChannels.register(pubSubMessenger);
        pluginChannels.register(new StructureChannel());
        pluginChannels.register(new RulesChannel());
        tickSpeed = new TickSpeed(server);
        loggers = new LoggerManager(server);
    }

    public static QuickCarpetServer init(MinecraftServer server) {
        return instance = new QuickCarpetServer(server);
    }

    public static void shutdown() {
        instance = null;
    }

    public static QuickCarpetServer getInstance() {
        if (instance == null) throw new IllegalStateException("No QuickCarpetServer instance");
        return instance;
    }

    @Nullable
    public static QuickCarpetServer getNullableInstance() {
        return instance;
    }

    public static MinecraftServer getMinecraftServer() {
        return getInstance().server;
    }

    @Nullable
    public static MinecraftServer getNullableMinecraftServer() {
        return instance == null ? null : instance.server;
    }

    public static Path getConfigFile(WorldSavePath name) {
        return getMinecraftServer().getSavePath(name);
    }

    @Override
    public ServerPluginChannelManager getPluginChannelManager() {
        return pluginChannels;
    }

    @Override
    public void tick(MinecraftServer server) {
        try {
            tickSpeed.tick();
            HUDController.update(server);
            QuickCarpet.PUBSUB.update(server.getTicks());
            StructureChannel.instance.tick();
        } catch (RuntimeException e) {
            LOG.error("Exception ticking " + Build.NAME, e);
        }
    }

    @Override
    public void onPlayerConnect(ServerPlayerEntity player) {
        try {
            loggers.onPlayerConnect(player);
            pluginChannels.onPlayerConnect(player);
        } catch (RuntimeException e) {
            LOG.error("Exception during onPlayerConnect for " + player.getEntityName(), e);
        }
    }

    @Override
    public void onPlayerDisconnect(ServerPlayerEntity player) {
        try {
            loggers.onPlayerDisconnect(player);
            pluginChannels.onPlayerDisconnect(player);
        } catch (RuntimeException e) {
            LOG.error("Exception during onPlayerDisconnect for " + player.getEntityName(), e);
        }
    }

    @Override
    public JsonObject getTelemetryData() {
        JsonObject obj = new JsonObject();
        JsonObject serverObj = new JsonObject();
        serverObj.addProperty("players", server.getCurrentPlayerCount());
        serverObj.addProperty("maxPlayers", server.getMaxPlayerCount());
        obj.add("server", serverObj);
        JsonArray worlds = new JsonArray();
        for (ServerWorld world : server.getWorlds()) {
            JsonObject worldObj = new JsonObject();
            worldObj.addProperty("name", ((ServerWorldProperties) world.getLevelProperties()).getLevelName());
            worldObj.addProperty("dimension", world.getRegistryKey().getValue().toString());
            worldObj.addProperty("loadedChunks", world.getChunkManager().getLoadedChunkCount());
            Map<SpawnGroup, Pair<Integer, Integer>> mobcaps = Mobcaps.getMobcaps(world);
            JsonObject mobcapsObj = new JsonObject();
            for (Map.Entry<SpawnGroup, Pair<Integer, Integer>> mobcap : mobcaps.entrySet()) {
                JsonObject mobcapObj = new JsonObject();
                mobcapObj.addProperty("current", mobcap.getValue().getLeft());
                mobcapObj.addProperty("max", mobcap.getValue().getRight());
                mobcapsObj.add(mobcap.getKey().getName(), mobcapObj);
            }
            worldObj.add("mobcaps", mobcapsObj);
            worlds.add(worldObj);
        }
        obj.add("worlds", worlds);
        obj.add("tickSpeed", tickSpeed.getTelemetryData());
        return obj;
    }
}
