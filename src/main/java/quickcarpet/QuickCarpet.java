package quickcarpet;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.registry.RegistryTracker;
import net.minecraft.world.level.ServerWorldProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import quickcarpet.commands.*;
import quickcarpet.helper.Mobcaps;
import quickcarpet.helper.TickSpeed;
import quickcarpet.logging.LoggerManager;
import quickcarpet.module.ModuleHost;
import quickcarpet.module.QuickCarpetModule;
import quickcarpet.network.PluginChannelManager;
import quickcarpet.network.channels.RulesChannel;
import quickcarpet.network.channels.StructureChannel;
import quickcarpet.pubsub.PubSubManager;
import quickcarpet.pubsub.PubSubMessenger;
import quickcarpet.pubsub.PubSubNode;
import quickcarpet.settings.Settings;
import quickcarpet.utils.*;
import quickcarpet.utils.extensions.WaypointContainer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class QuickCarpet implements ModuleHost, ServerEventListener, TelemetryProvider {
    private static final Logger LOG = LogManager.getLogger();
    public static final PubSubManager PUBSUB = new PubSubManager();

    private static QuickCarpet instance = new QuickCarpet();

    public static MinecraftServer minecraft_server;
    public RegistryTracker.Modifiable dimensionTracker;
    public TickSpeed tickSpeed;

    @Environment(EnvType.CLIENT)
    public QuickCarpetClient client;
    public PluginChannelManager pluginChannels;
    public final Set<QuickCarpetModule> modules = new TreeSet<>();
    private final PubSubMessenger pubSubMessenger = new PubSubMessenger(PUBSUB);
    private CommandDispatcher<ServerCommandSource> dispatcher;
    public LoggerManager loggers;

    @SuppressWarnings("UnstableApiUsage")
    private Multimap<ServerWorld, Runnable> worldUnloadCallbacks = MultimapBuilder.hashKeys().arrayListValues().build();

    // Fabric on dedicated server will call getInstance at return of DedicatedServer::<init>(...)
    // new CommandManager(...) is before that so QuickCarpet is created from that
    // Client will call getInstance at head of MinecraftClient::init()
    private QuickCarpet() {
        instance = this;
    }

    public static QuickCarpet getInstance() {
        return instance;
    }

    @Environment(EnvType.CLIENT)
    public static void initClient() {
        instance.client = new QuickCarpetClient();
    }

    @Override
    public void onServerInit(MinecraftServer server) {
        minecraft_server = server;
        tickSpeed = new TickSpeed(false);
        loggers = new LoggerManager();
        pluginChannels = new PluginChannelManager(server);
        pluginChannels.register(pubSubMessenger);
        pluginChannels.register(new StructureChannel());
        pluginChannels.register(new RulesChannel());
        for (QuickCarpetModule m : modules) m.onServerInit(server);
    }

    @Override
    public void onServerLoaded(MinecraftServer server) {
        Settings.MANAGER.init(server);
        for (QuickCarpetModule m : modules) m.onServerLoaded(server);
        registerCommands(dispatcher);
    }

    @Override
    public void tick(MinecraftServer server) {
        try {
            tickSpeed.tick(server);
            HUDController.update(server);
            PUBSUB.update(server.getTicks());
            StructureChannel.instance.tick();
        } catch (RuntimeException e) {
            LOG.error("Exception ticking " + Build.NAME, e);
        }
        for (QuickCarpetModule m : modules) {
            try {
                m.tick(server);
            } catch (RuntimeException e) {
                LOG.error("Exception ticking " + Build.NAME + " module " + m.getName(), e);
            }
        }
    }

    @Override
    public void onGameStarted(EnvType env) {
        CarpetRegistry.init();
        CarpetProfiler.init();
        try {
            Translations.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Settings.MANAGER.parse();
        for (QuickCarpetModule m : modules) {
            m.onGameStarted();
            LOG.info(Build.NAME + " module " + m.getId() + " version " + m.getVersion() + " initialized");
        }
    }

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        CarpetCommand.register(dispatcher);
        TickCommand.register(dispatcher);
        CarpetFillCommand.register(dispatcher);
        CarpetCloneCommand.register(dispatcher);
        CarpetSetBlockCommand.register(dispatcher);
        CounterCommand.register(dispatcher);
        PlayerCommand.register(dispatcher);
        LogCommand.register(dispatcher);
        SpawnCommand.register(dispatcher);
        PingCommand.register(dispatcher);
        CameraModeCommand.register(dispatcher);
        MeasureCommand.register(dispatcher);
        WaypointCommand.register(dispatcher);
        TelemetryCommand.register(dispatcher);
        for (QuickCarpetModule m : modules) m.registerCommands(dispatcher);
    }

    public void setCommandDispatcher(CommandDispatcher<ServerCommandSource> dispatcher) {
        if (this.dispatcher != null) {
            // reload
            registerCommands(dispatcher);
        }
        this.dispatcher = dispatcher;
    }

    @Override
    public void registerModule(QuickCarpetModule module) {
        LOG.info(Build.NAME + " module " + module.getId() + " version " + module.getVersion() + " registered");
        modules.add(module);
        try {
            Translations.loadModuleTranslations(module);
        } catch (IOException e) {
            e.printStackTrace();
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
        for (QuickCarpetModule m : modules) {
            try {
                m.onPlayerConnect(player);
            } catch (RuntimeException e) {
                LOG.error("Exception during onPlayerConnect for " + player.getEntityName() + " in module " + m.getName(), e);
            }
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
        for (QuickCarpetModule m : modules) {
            try {
                m.onPlayerDisconnect(player);
            } catch (RuntimeException e) {
                LOG.error("Exception during onPlayerDisconnect for " + player.getEntityName() + " in module " + m.getName(), e);
            }
        }
    }

    @Override
    public void onWorldsLoaded(MinecraftServer server) {
        for (ServerWorld world : server.getWorlds()) onWorldLoaded(world);
        for (QuickCarpetModule m : modules) m.onWorldsLoaded(server);
    }

    @Override
    public void onWorldLoaded(ServerWorld world) {
        try {
            Map<String, Waypoint> waypoints = ((WaypointContainer) world).getWaypoints();
            waypoints.clear();
            waypoints.putAll(Waypoint.loadWaypoints((WaypointContainer) world));
            Identifier dimType = world.getRegistryKey().getValue();
            String prefix = dimType.getNamespace() + "." + dimType.getPath() + ".mob_cap";
            PubSubNode mobCapNode = PUBSUB.getOrCreateNode(prefix);
            for (SpawnGroup category : SpawnGroup.values()) {
                PUBSUB.addKnownNode(mobCapNode.getOrCreateChildNode(category.getName(), "filled"));
                PUBSUB.addKnownNode(mobCapNode.getOrCreateChildNode(category.getName(), "total"));
            }
            PubSubManager.CallbackHandle handle = PUBSUB.addCallback(mobCapNode, 20, node -> {
                Map<SpawnGroup, Pair<Integer, Integer>> mobcaps = Mobcaps.getMobcaps(world);
                for (Map.Entry<SpawnGroup, Pair<Integer, Integer>> entry : mobcaps.entrySet()) {
                    PubSubNode categoryNode = node.getOrCreateChildNode(entry.getKey().getName());
                    PUBSUB.publish(categoryNode.getOrCreateChildNode("filled"), entry.getValue().getLeft());
                    PUBSUB.publish(categoryNode.getOrCreateChildNode("total"), entry.getValue().getRight());
                }
            });
            worldUnloadCallbacks.put(world, handle::remove);
        } catch (Exception e) {
            LOG.error("Error loading waypoints for " + ((ServerWorldProperties) world.getLevelProperties()).getLevelName() + "/" + world.getRegistryKey().getValue());
        }
    }

    @Override
    public void onWorldsSaved(MinecraftServer server) {
        for (ServerWorld world : server.getWorlds()) onWorldSaved(world);
        for (QuickCarpetModule m : modules) m.onWorldsSaved(server);
    }

    @Override
    public void onWorldSaved(ServerWorld world) {
        try {
            Waypoint.saveWaypoints((WaypointContainer) world);
        } catch (Exception e) {
            LOG.error("Error saving waypoints for " + ((ServerWorldProperties) world.getLevelProperties()).getLevelName() + "/" + world.getRegistryKey().getValue());
        }
    }

    @Override
    public void onWorldsUnloaded(MinecraftServer server) {
        for (ServerWorld world : server.getWorlds()) onWorldUnloaded(world);
        for (QuickCarpetModule m : modules) m.onWorldsUnloaded(server);
    }

    @Override
    public void onWorldUnloaded(ServerWorld world) {
        Collection<Runnable> callbacks = worldUnloadCallbacks.removeAll(world);
        for (Runnable r : callbacks) r.run();
    }

    @Override
    public boolean isIgnoredForRegistrySync(Identifier registry, Identifier entry) {
        if (CarpetRegistry.isIgnoredForSync(entry)) return true;
        for (QuickCarpetModule m : modules) if (m.isIgnoredForRegistrySync(registry, entry)) return true;
        return false;
    }

    public static boolean isDevelopment() {
        return Build.VERSION.contains("dev") || FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    public static Path getConfigFile(WorldSavePath name) {
        return minecraft_server.getSavePath(name);
    }

    @Override
    public JsonObject getTelemetryData() {
        JsonObject obj = new JsonObject();
        JsonObject server = new JsonObject();
        server.addProperty("players", minecraft_server.getCurrentPlayerCount());
        server.addProperty("maxPlayers", minecraft_server.getMaxPlayerCount());
        obj.add("server", server);
        JsonArray worlds = new JsonArray();
        for (ServerWorld world : minecraft_server.getWorlds()) {
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
