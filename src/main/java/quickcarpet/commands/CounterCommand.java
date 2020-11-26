package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import quickcarpet.helper.HopperCounter;
import quickcarpet.settings.Settings;

import static net.minecraft.server.command.CommandManager.literal;
import static quickcarpet.utils.Messenger.*;

public class CounterCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> counter = literal("counter")
                .requires((player) -> Settings.hopperCounters)
                .executes((context) -> listAllCounters(context.getSource(), false));

        counter.then(literal("reset").executes(c-> resetCounter(c.getSource(), null)));
        counter.then(literal("realtime").executes(c -> listAllCounters(c.getSource(), true)));

        counter.then(Utils.argument("key", HopperCounter.Key.class)
                .executes(c -> displayCounter(c.getSource(), getKey(c, "key"), false))
                .then(literal("reset").executes(c -> resetCounter(c.getSource(), getKey(c, "key"))))
                .then(literal("realtime").executes((c) -> displayCounter(c.getSource(), getKey(c, "key"), true))));
        dispatcher.register(counter);
    }

    private static HopperCounter.Key getKey(CommandContext<ServerCommandSource> ctx, String name) throws CommandSyntaxException {
        return Utils.getArgument(ctx, name, HopperCounter.Key.class);
    }

    private static int displayCounter(ServerCommandSource source, HopperCounter.Key key, boolean realtime) {
        HopperCounter counter = HopperCounter.getCounter(key);
        if (counter == null) throw new CommandException(t("command.counter.unknown"));
        for (Text message : counter.format(source.getMinecraftServer(), realtime, false)) {
            m(source, message);
        }
        return 1;
    }

    private static int resetCounter(ServerCommandSource source, HopperCounter.Key color) {
        if (color == null) {
            HopperCounter.resetAll(source.getMinecraftServer());
            m(source, t("command.counter.reset.success"));
        } else {
            HopperCounter counter = HopperCounter.getCounter(color);
            if (counter == null) throw new CommandException(t("command.counter.unknown"));
            counter.reset(source.getMinecraftServer());
            m(source, t("command.counter.reset.one.success", counter.key.getText()));
        }
        return 1;
    }

    private static int listAllCounters(ServerCommandSource source, boolean realtime) {
        send(source, HopperCounter.formatAll(source.getMinecraftServer(), realtime));
        return 1;
    }
}
