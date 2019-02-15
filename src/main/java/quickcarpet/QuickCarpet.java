package quickcarpet;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import quickcarpet.commands.CarpetCommand;

public class QuickCarpet implements ModInitializer {

    @Override
    public void onInitialize() {

    }

    public static MinecraftServer minecraft_server;

    public static void init(MinecraftServer server) //Constructor of this static single ton class
    {
        QuickCarpet.minecraft_server = server;
    }

    public static void onServerLoaded(MinecraftServer server)
    {
        QuickCarpetSettings.apply_settings_from_conf(server);
    }

    public static void registerCarpetCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        CarpetCommand.register(dispatcher);
    }
}
