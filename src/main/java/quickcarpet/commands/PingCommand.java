package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import quickcarpet.settings.Settings;
import quickcarpet.utils.Constants.PingCommand.Keys;

import static net.minecraft.server.command.CommandManager.literal;
import static quickcarpet.utils.Messenger.*;

public class PingCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var ping = literal("ping")
            .requires(s -> s.hasPermissionLevel(Settings.commandPing))
            .executes(c -> {
                ServerCommandSource source = c.getSource();
                ServerPlayerEntity player = source.getPlayer();
                int pingMs = player.pingMilliseconds;
                m(source, t(Keys.RESULT, s(Integer.toString(pingMs), getHeatmapColor(pingMs, 250))));
                return 1;
            });
        dispatcher.register(ping);
    }
}
