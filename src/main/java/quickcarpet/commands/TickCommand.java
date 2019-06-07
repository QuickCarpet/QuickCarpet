package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import quickcarpet.helper.TickSpeed;
import quickcarpet.settings.Settings;
import quickcarpet.utils.CarpetProfiler;
import quickcarpet.utils.Messenger;

import static com.mojang.brigadier.arguments.FloatArgumentType.floatArg;
import static com.mojang.brigadier.arguments.FloatArgumentType.getFloat;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandSource.suggestMatching;

public class TickCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> literalargumentbuilder = literal("tick").
                requires((player) -> Settings.commandTick).
                then(literal("rate").
                        executes((c) -> queryTps(c.getSource())).
                        then(argument("rate", floatArg(0.1F, 500.0F)).
                                suggests( (c, b) -> suggestMatching(new String[]{"20.0"},b)).
                                executes((c) -> setTps(c.getSource(), getFloat(c, "rate"))))).
                then(literal("warp").
                        executes( (c)-> setWarp(c.getSource(), 0, null)).
                        then(argument("ticks", integer(0,4000000)).
                                suggests( (c, b) -> suggestMatching(new String[]{"3600","72000"},b)).
                                executes((c) -> setWarp(c.getSource(), getInteger(c,"ticks"), null)).
                                then(argument("tail command", greedyString()).
                                        executes( (c) -> setWarp(
                                                c.getSource(),
                                                getInteger(c,"ticks"),
                                                getString(c, "tail command")))))).
        /*
                then(literal("freeze").executes( (c)-> toggleFreeze(c.getSource()))).
                then(literal("step").
                        executes((c) -> step(1)).
                        then(argument("ticks", integer(1,72000)).
                                suggests( (c, b) -> CommandSource.suggestMatching(new String[]{"20"},b)).
                                executes((c) -> step(getInteger(c,"ticks"))))).
                then(literal("superHot").executes( (c)-> toggleSuperHot(c.getSource()))).
        */
                then(literal("health").
                        executes( (c) -> healthReport(c.getSource(), 100)).
                        then(argument("ticks", integer(20,24000)).
                                executes( (c) -> healthReport(c.getSource(), getInteger(c, "ticks"))))).
                then(literal("entities").
                        executes((c) -> healthEntities(c.getSource(), 100)).
                        then(argument("ticks", integer(20,24000)).
                                executes((c) -> healthEntities(c.getSource(), getInteger(c, "ticks"))))).
                then(literal("measure").
                        executes(c -> measureCurrent(c.getSource())).
                        then(argument("ticks", integer(10, 24000)).
                                executes(c -> measure(c.getSource(), getInteger(c, "ticks")))))
                ;

        dispatcher.register(literalargumentbuilder);
    }
    
    private static int setTps(ServerCommandSource source, float tps)
    {
        TickSpeed.tickrate(tps);
        queryTps(source);
        return (int)tps;
    }
    
    private static int queryTps(ServerCommandSource source)
    {
        Messenger.m(source, "w Current tps is: ",String.format("wb %.1f", TickSpeed.tickrate));
        return (int)TickSpeed.tickrate;
    }
    
    private static int setWarp(ServerCommandSource source, int advance, String tail_command)
    {
        PlayerEntity player = null;
        try
        {
            player = source.getPlayer();
        }
        catch (CommandSyntaxException ignored)
        {
        }
        Text message = TickSpeed.tickrate_advance(player, advance, tail_command, source);
        if (message != null)
        {
            source.sendFeedback(message, false);
        }
        return 1;
    }

    private static int healthReport(CommandSource source, int ticks)
    {
        CarpetProfiler.startTickReport(CarpetProfiler.ReportType.HEALTH, ticks);
        return 1;
    }

    private static int healthEntities(CommandSource source, int ticks)
    {
        CarpetProfiler.startTickReport(CarpetProfiler.ReportType.ENTITIES, ticks);
        return 1;
    }

    private static int measureCurrent(ServerCommandSource source) {
        printMSPTStats(source, TickSpeed.getMSPTStats());
        return 1;
    }

    private static int measure(ServerCommandSource source, int ticks) {
        TickSpeed.startMeasurement(source, ticks);
        return 1;
    }

    public static void printMSPTStats(ServerCommandSource source, TickSpeed.MSPTStatistics stats) {
        Messenger.m(source, "e Statistics collected over ", "c " + stats.count + " ", "e ticks", "g :");
        Messenger.m(source, "w Load average (1m/5m/15m) [mspt]", "g : ",
            String.format("c %.3f", TickSpeed.getExponential1MinuteMSPT()), "g , ",
            String.format("c %.3f", TickSpeed.getExponential5MinuteMSPT()), "g , ",
            String.format("c %.3f", TickSpeed.getExponential15MinuteMSPT())
        );
        Messenger.m(source, "w min, avg, max [mspt]", "g : ",
                String.format("c %.3f", stats.min), "g , ",
                String.format("c %.3f±%.3f", stats.mean, stats.stdDev), "g , ",
                String.format("c %.3f", stats.max)
        );
        Messenger.m(source, "w Ticks >50ms", "g : ", String.format("c %.1f%%", stats.lagPercentage));
        Messenger.m(source, "w 90th%, 95th%, 99th% [mspt]", "g : ",
                String.format("c %.3f", stats.percentile90), "g , ",
                String.format("c %.3f", stats.percentile95), "g , ",
                String.format("c %.3f", stats.percentile99)
                );
    }
}
