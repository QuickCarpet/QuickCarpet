package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TextComponent;
import net.minecraft.util.DyeColor;
import quickcarpet.QuickCarpetSettings;
import quickcarpet.helper.HopperCounter;
import quickcarpet.utils.Messenger;

public class CounterCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        LiteralArgumentBuilder<ServerCommandSource> literalargumentbuilder = CommandManager.literal("counter").executes((context)
                -> listAllCounters(context.getSource(), false)).requires((player) ->
                QuickCarpetSettings.getBool("hopperCounters"));

        literalargumentbuilder.
                then((CommandManager.literal("reset").executes( (p_198489_1_)->
                        resetCounter(p_198489_1_.getSource(), null))));
        for (DyeColor enumDyeColor: DyeColor.values())
        {
            String color = enumDyeColor.toString();
            literalargumentbuilder.
                    then((CommandManager.literal(color).executes( (p_198489_1_)-> displayCounter(p_198489_1_.getSource(), color, false))));
            literalargumentbuilder.then(CommandManager.literal(color).
                    then(CommandManager.literal("reset").executes((context) ->
                            resetCounter(context.getSource(), color))));
            literalargumentbuilder.then(CommandManager.literal(color).
                    then(CommandManager.literal("realtime").executes((context) ->
                            displayCounter(context.getSource(), color, true))));
        }
        dispatcher.register(literalargumentbuilder);
    }

    private static int displayCounter(ServerCommandSource source, String color, boolean realtime)
    {
        for (TextComponent message: HopperCounter.query_hopper_stats_for_color(source.getMinecraftServer(), color, realtime, false))
        {
            source.sendFeedback(message, false);
        }
        return 1;
    }

    private static int resetCounter(ServerCommandSource source, String color)
    {
        HopperCounter.reset_hopper_counter(source.getMinecraftServer(), color);
        if (color == null)
        {
            Messenger.m(source, "w Restarted all counters");
        }
        else
        {
            Messenger.m(source, "w Restarted "+color+" counter");
        }
        return 1;
    }

    private static int listAllCounters(ServerCommandSource source, boolean realtime)
    {
        for (TextComponent message: HopperCounter.query_hopper_all_stats(source.getMinecraftServer(), realtime))
        {
            source.sendFeedback(message, false);
        }
        return 1;
    }

}
