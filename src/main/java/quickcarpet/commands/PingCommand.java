package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import quickcarpet.settings.Settings;

import static net.minecraft.server.command.CommandManager.literal;
import static quickcarpet.utils.Messenger.*;

public class PingCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> ping = literal("ping")
            .requires(s -> s.hasPermissionLevel(Settings.commandPing))
            .executes(c -> {
                ServerPlayerEntity player = c.getSource().getPlayer();
                int pingMs = player.pingMilliseconds;
                m(player, t("command.ping.result", s(Integer.toString(pingMs), getHeatmapColor(pingMs, 250))));
                return 1;
            });
        dispatcher.register(ping);
    }
}
