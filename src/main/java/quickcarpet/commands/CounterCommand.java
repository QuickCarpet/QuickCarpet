package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import quickcarpet.helper.HopperCounter;
import quickcarpet.settings.Settings;
import quickcarpet.utils.Constants.CounterCommand.Keys;

import static net.minecraft.server.command.CommandManager.literal;
import static quickcarpet.utils.Constants.CounterCommand.Texts.RESET_SUCCESS;
import static quickcarpet.utils.Messenger.*;

public class CounterCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var counter = literal("counter")
            .requires(p -> Settings.hopperCounters)
            .executes(c -> listAllCounters(c.getSource(), false));

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
        for (Text message : counter.format(source.getServer(), realtime, false)) {
            m(source, message);
        }
        return 1;
    }

    private static int resetCounter(ServerCommandSource source, HopperCounter.Key color) {
        if (color == null) {
            HopperCounter.getCounter(HopperCounter.Key.ALL).reset(source.getServer());
            m(source, RESET_SUCCESS);
        } else {
            HopperCounter counter = HopperCounter.getCounter(color);
            counter.reset(source.getServer());
            m(source, t(Keys.RESET_ONE_SUCCESS, counter.key.getText()));
        }
        return 1;
    }

    private static int listAllCounters(ServerCommandSource source, boolean realtime) {
        send(source, HopperCounter.formatAll(source.getServer(), realtime));
        return 1;
    }
}
