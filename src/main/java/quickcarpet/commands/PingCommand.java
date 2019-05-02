package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import quickcarpet.QuickCarpetSettings;
import quickcarpet.utils.Messenger;

import static net.minecraft.server.command.CommandManager.literal;

public class PingCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = literal("ping").
            requires((player) -> QuickCarpetSettings.getBool("commandPing")).
            executes(c -> {
                ServerPlayerEntity player = c.getSource().getPlayer();
                int ping = player.field_13967;
                player.sendMessage(Messenger.c("w Your ping is ", Messenger.heatmap_color(ping, 250) + " " + ping, "w  ms"));
                return 1;
            });
        dispatcher.register(builder);
    }
}
