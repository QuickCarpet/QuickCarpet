package quickcarpet;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import quickcarpet.commands.*;
import quickcarpet.helper.TickSpeed;
import quickcarpet.logging.LoggerRegistry;
import quickcarpet.network.PluginChannelManager;
import quickcarpet.network.channels.StructureChannel;
import quickcarpet.pubsub.PubSubManager;
import quickcarpet.pubsub.PubSubMessenger;
import quickcarpet.settings.Settings;
import quickcarpet.utils.CarpetRegistry;
import quickcarpet.utils.HUDController;

public class QuickCarpet implements ModInitializer {
    public static MinecraftServer minecraft_server;
    public static PluginChannelManager pluginChannels;
    public static final PubSubManager PUBSUB = new PubSubManager();
    public static final PubSubMessenger PUBSUB_MESSENGER = new PubSubMessenger(PUBSUB);

    public static void init(MinecraftServer server) //Constructor of this static single ton class
    {
        minecraft_server = server;
        pluginChannels = new PluginChannelManager(server);
        pluginChannels.register(PUBSUB_MESSENGER);
        pluginChannels.register(new StructureChannel());
    }

    public static void onServerLoaded(MinecraftServer server) {
        Settings.MANAGER.init(server);
    }

    public static void tick(MinecraftServer server) {
        TickSpeed.tick(server);
        HUDController.update_hud(server);
        StructureChannel.instance.tick();
    }
    
    public static void onGameStarted() {
        LoggerRegistry.initLoggers();
        CarpetRegistry.init();
    }

    public static void registerCarpetCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
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
    }

    @Override
    public void onInitialize() {

    }
}
