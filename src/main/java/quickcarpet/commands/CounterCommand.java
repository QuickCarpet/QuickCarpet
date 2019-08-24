package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
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
        for (DyeColor enumDyeColor: DyeColor.values()) {
            String color = enumDyeColor.toString();
            counter.then(literal(color).executes(c -> displayCounter(c.getSource(), color, false)));
            counter.then(literal(color).then(literal("reset").executes(c -> resetCounter(c.getSource(), color))));
            counter.then(literal(color).then(literal("realtime").executes((c) -> displayCounter(c.getSource(), color, true))));
        }
        dispatcher.register(counter);
    }

    private static int displayCounter(ServerCommandSource source, String color, boolean realtime) {
        HopperCounter counter = HopperCounter.getCounter(color);
        if (counter == null) throw new CommandException(t("command.counter.unknownColor"));
        for (Text message : counter.format(source.getMinecraftServer(), realtime, false)) {
            m(source, message);
        }
        return 1;
    }

    private static int resetCounter(ServerCommandSource source, String color) {
        if (color == null) {
            HopperCounter.resetAll(source.getMinecraftServer());
            m(source, t("command.counter.reset.success"));
        } else {
            HopperCounter counter = HopperCounter.getCounter(color);
            if (counter == null) throw new CommandException(t("command.counter.unknownColor"));
            counter.reset(source.getMinecraftServer());
            m(source, t("command.counter.reset.one.success", t("color.minecraft." + counter.color.getName())));
        }
        return 1;
    }

    private static int listAllCounters(ServerCommandSource source, boolean realtime) {
        send(source, HopperCounter.formatAll(source.getMinecraftServer(), realtime));
        return 1;
    }
}
