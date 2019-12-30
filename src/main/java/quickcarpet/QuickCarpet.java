package quickcarpet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EntityCategory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
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
import quickcarpet.settings.Settings;
import quickcarpet.utils.*;
import quickcarpet.utils.extensions.WaypointContainer;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class QuickCarpet implements ModInitializer, ModuleHost, ServerEventListener, TelemetryProvider {
    private static final Logger LOG = LogManager.getLogger();
    public static final PubSubManager PUBSUB = new PubSubManager();

    private static QuickCarpet instance = new QuickCarpet();

    public static MinecraftServer minecraft_server;
    public TickSpeed tickSpeed;

    @Environment(EnvType.CLIENT)
    public QuickCarpetClient client;
    public PluginChannelManager pluginChannels;
    public final Set<QuickCarpetModule> modules = new TreeSet<>();
    private final PubSubMessenger pubSubMessenger = new PubSubMessenger(PUBSUB);
    private CommandDispatcher<ServerCommandSource> dispatcher;
    public LoggerManager loggers;

    // Fabric on dedicated server will call getInstance at return of DedicatedServer::<init>(...)
    // new CommandManager(...) is before that so QuickCarpet is created from that
    // Client will call getInstance at head of MinecraftClient::init()
    public QuickCarpet() {
        instance = this;
    }

    public static QuickCarpet getInstance() {
        return instance;
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
        tickSpeed.tick(server);
        HUDController.update(server);
        PUBSUB.update(server.getTicks());
        StructureChannel.instance.tick();
        for (QuickCarpetModule m : modules) m.tick(server);
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
        if (env == EnvType.CLIENT) {
            this.client = new QuickCarpetClient();
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
        loggers.onPlayerConnect(player);
        pluginChannels.onPlayerConnect(player);
        for (QuickCarpetModule m : modules) m.onPlayerConnect(player);
    }

    @Override
    public void onPlayerDisconnect(ServerPlayerEntity player) {
        loggers.onPlayerDisconnect(player);
        pluginChannels.onPlayerDisconnect(player);
        for (QuickCarpetModule m : modules) m.onPlayerDisconnect(player);
    }

    @Override
    public void onWorldLoaded(ServerWorld world) {
        try {
            Map<String, Waypoint> waypoints = ((WaypointContainer) world).getWaypoints();
            waypoints.clear();
            waypoints.putAll(Waypoint.loadWaypoints((WaypointContainer) world));
        } catch (Exception e) {
            LOG.error("Error loading waypoints for " + world.getLevelProperties().getLevelName() + "/" + world.getDimension().getType());
        }
    }

    @Override
    public void onWorldSaved(ServerWorld world) {
        try {
            Waypoint.saveWaypoints((WaypointContainer) world);
        } catch (Exception e) {
            LOG.error("Error saving waypoints for " + world.getLevelProperties().getLevelName() + "/" + world.getDimension().getType());
        }
    }

    @Override
    public void onInitialize() {

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

    public static File getConfigFile(String name) {
        return minecraft_server.getLevelStorage().resolveFile(minecraft_server.getLevelName(), name);
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
            worldObj.addProperty("name", world.getLevelProperties().getLevelName());
            worldObj.addProperty("dimension", world.getDimension().getType().toString());
            worldObj.addProperty("loadedChunks", world.getChunkManager().getLoadedChunkCount());
            Map<EntityCategory, Pair<Integer, Integer>> mobcaps = Mobcaps.getMobcaps(world);
            JsonObject mobcapsObj = new JsonObject();
            for (Map.Entry<EntityCategory, Pair<Integer, Integer>> mobcap : mobcaps.entrySet()) {
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
