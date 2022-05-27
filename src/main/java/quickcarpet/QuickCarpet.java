package quickcarpet;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.world.level.ServerWorldProperties;
import org.slf4j.Logger;
import quickcarpet.api.QuickCarpetAPI;
import quickcarpet.api.ServerEventListener;
import quickcarpet.api.TelemetryProvider;
import quickcarpet.api.module.QuickCarpetModule;
import quickcarpet.commands.*;
import quickcarpet.helper.Mobcaps;
import quickcarpet.pubsub.PubSubManager;
import quickcarpet.pubsub.PubSubNode;
import quickcarpet.settings.Settings;
import quickcarpet.utils.*;
import quickcarpet.utils.extensions.WaypointContainer;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class QuickCarpet implements QuickCarpetAPI, ServerEventListener, TelemetryProvider {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final PubSubManager PUBSUB = new PubSubManager();

    private static QuickCarpet instance = new QuickCarpet();

    public final Set<QuickCarpetModule> modules = new TreeSet<>();
    private QuickCarpetServer server;
    private CommandDispatcher<ServerCommandSource> dispatcher;
    private final Multimap<ServerWorld, Runnable> worldUnloadCallbacks = MultimapBuilder.hashKeys().arrayListValues().build();

    // Fabric on dedicated server will call getInstance at return of DedicatedServer::<init>(...)
    // new CommandManager(...) is before that so QuickCarpet is created from that
    // Client will call getInstance at head of MinecraftClient::init()
    private QuickCarpet() {
        instance = this;
    }

    public static QuickCarpet getInstance() {
        return instance;
    }

    public void onBootstrapInitialize() {
        CarpetRegistry.init();
        QuickCarpetRegistries.init();
    }

    @Override
    public void onServerInit(MinecraftServer server) {
        this.server = QuickCarpetServer.init(server);
        for (QuickCarpetModule m : modules) m.onServerInit(server);
    }

    @Override
    public void onServerLoaded(MinecraftServer server) {
        Settings.MANAGER.init(server);
        this.server.onServerLoaded(server);
        for (QuickCarpetModule m : modules) m.onServerLoaded(server);
        registerCommands(dispatcher);
    }

    @Override
    public void tick(MinecraftServer server) {
        this.server.tick(server);
        for (QuickCarpetModule m : modules) {
            try {
                m.tick(server);
            } catch (RuntimeException e) {
                LOGGER.error("Exception ticking " + Build.NAME + " module " + m.getName(), e);
            }
        }
    }

    @Override
    public void onGameStarted(EnvType env) {
        QuickCarpetAPI.getInstance();
        CarpetProfiler.init();
        try {
            Translations.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Settings.MANAGER.parse();
        for (QuickCarpetModule m : modules) {
            m.onGameStarted();
            LOGGER.info(Build.NAME + " module " + m.getId() + " version " + m.getVersion() + " initialized");
        }
    }

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        VanillaCommandAddons.register(dispatcher);
        CarpetCommand.register(dispatcher);
        TickCommand.register(dispatcher);
        CounterCommand.register(dispatcher);
        PlayerCommand.register(dispatcher);
        LogCommand.register(dispatcher);
        SpawnCommand.register(dispatcher);
        PingCommand.register(dispatcher);
        CameraModeCommand.register(dispatcher);
        MeasureCommand.register(dispatcher);
        WaypointCommand.register(dispatcher);
        TelemetryCommand.register(dispatcher);
        BlockInfoCommand.register(dispatcher);
        FluidInfoCommand.register(dispatcher);
        FixCommand.register(dispatcher);
        DataTrackerCommand.register(dispatcher);
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
        LOGGER.info(Build.NAME + " module " + module.getId() + " version " + module.getVersion() + " registered");
        modules.add(module);
        try {
            Translations.loadModuleTranslations(module);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPlayerConnect(ServerPlayerEntity player) {
        server.onPlayerConnect(player);
        for (QuickCarpetModule m : modules) {
            try {
                m.onPlayerConnect(player);
            } catch (RuntimeException e) {
                LOGGER.error("Exception during onPlayerConnect for " + player.getEntityName() + " in module " + m.getName(), e);
            }
        }
    }

    @Override
    public void onPlayerDisconnect(ServerPlayerEntity player) {
        server.onPlayerDisconnect(player);
        for (QuickCarpetModule m : modules) {
            try {
                m.onPlayerDisconnect(player);
            } catch (RuntimeException e) {
                LOGGER.error("Exception during onPlayerDisconnect for " + player.getEntityName() + " in module " + m.getName(), e);
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
        try {
            Map<String, Waypoint> waypoints = ((WaypointContainer) world).quickcarpet$getWaypoints();
            waypoints.clear();
            waypoints.putAll(Waypoint.loadWaypoints((WaypointContainer) world));
        } catch (Exception e) {
            LOGGER.error("Error loading waypoints for {}/{}", ((ServerWorldProperties) world.getLevelProperties()).getLevelName(), world.getRegistryKey().getValue(), e);
        }
    }

    @Override
    public void onWorldsSaved(MinecraftServer server) {
        for (ServerWorld world : server.getWorlds()) onWorldSaved(world);
        if (this.server != null) this.server.onWorldsSaved(server);
        for (QuickCarpetModule m : modules) m.onWorldsSaved(server);
    }

    @Override
    public void onWorldSaved(ServerWorld world) {
        try {
            Waypoint.saveWaypoints((WaypointContainer) world);
        } catch (Exception e) {
            LOGGER.error("Error saving waypoints for {}/{}", ((ServerWorldProperties) world.getLevelProperties()).getLevelName(), world.getRegistryKey().getValue(), e);
        }
    }

    @Override
    public void onWorldsUnloaded(MinecraftServer server) {
        for (ServerWorld world : server.getWorlds()) onWorldUnloaded(world);
        for (QuickCarpetModule m : modules) m.onWorldsUnloaded(server);
        this.server = null;
        QuickCarpetServer.shutdown();
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
        try {
            return Build.VERSION_IS_DEV || FabricLoader.getInstance().isDevelopmentEnvironment();
        } catch (NullPointerException e) {
            return true;
        }
    }

    @Override
    public JsonObject getTelemetryData() {
        return server.getTelemetryData();
    }

    @Override
    public String getVersion() {
        return Build.VERSION;
    }

    public static String getFullVersionString() {
        if (Build.VERSION_IS_DEV || isDevelopment()) {
            return Build.VERSION +  " " + Build.BRANCH + "-" + Build.COMMIT_SHORT + " (" + Build.BUILD_TIMESTAMP + ")";
        }
        return Build.VERSION;
    }

    public static class Provider implements QuickCarpetAPI.Provider {
        @Override
        public QuickCarpetAPI getInstance() {
            return QuickCarpet.getInstance();
        }
    }
}
