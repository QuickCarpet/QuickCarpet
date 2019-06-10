package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import quickcarpet.settings.Settings;
import quickcarpet.utils.Messenger;

import static net.minecraft.server.command.CommandManager.literal;

public class PingCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> ping = literal("ping")
            .requires((player) -> Settings.commandPing)
            .executes(c -> {
                ServerPlayerEntity player = c.getSource().getPlayer();
                int pingMs = player.field_13967;
                player.sendMessage(Messenger.c("w Your ping is ", Messenger.heatmap_color(pingMs, 250) + " " + pingMs, "w  ms"));
                return 1;
            });
        dispatcher.register(ping);
    }
}
