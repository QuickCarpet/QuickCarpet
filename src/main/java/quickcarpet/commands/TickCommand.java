package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockBox;
import quickcarpet.feature.TickSpeed;
import quickcarpet.settings.Settings;
import quickcarpet.utils.CarpetProfiler;
import quickcarpet.utils.CarpetProfiler.ReportBoundingBox;
import quickcarpet.utils.Constants.TickCommand.Keys;

import javax.annotation.Nullable;
import java.util.function.ToIntBiFunction;

import static com.mojang.brigadier.arguments.FloatArgumentType.floatArg;
import static com.mojang.brigadier.arguments.FloatArgumentType.getFloat;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.command.CommandSource.suggestMatching;
import static net.minecraft.command.argument.BlockPosArgumentType.blockPos;
import static net.minecraft.command.argument.BlockPosArgumentType.getBlockPos;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static quickcarpet.utils.Constants.TickCommand.Texts.*;
import static quickcarpet.utils.Messenger.*;

public class TickCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var tick = literal("tick")
            .requires(s -> s.hasPermissionLevel(Settings.commandTick))
            .then(literal("rate").requires(s -> s.hasPermissionLevel(Settings.commandTickManipulate))
                .executes(c -> sendCurrentTPS(c.getSource()))
                .then(argument("rate", floatArg(0.1F))
                    .suggests((c, b) -> suggestMatching(new String[]{"20"},b))
                    .executes(c -> setTps(c.getSource(), getFloat(c, "rate")))))
            .then(literal("warp").requires(s -> s.hasPermissionLevel(Settings.commandTickManipulate))
                .executes(c-> displayStatus(c.getSource()))
                .then(argument("ticks", integer(0,4000000))
                    .suggests((c, b) -> suggestMatching(new String[]{"3600","72000"},b))
                    .executes(c -> setWarp(c.getSource(), getInteger(c,"ticks"), null))
                    .then(argument("tail command", greedyString())
                        .executes(c -> setWarp(c.getSource(), getInteger(c,"ticks"), getString(c, "tail command"))))))
            .then(literal("freeze").requires(s -> s.hasPermissionLevel(Settings.commandTickManipulate))
                .executes( (c)-> toggleFreeze(c.getSource())))
            .then(literal("step").requires(s -> s.hasPermissionLevel(Settings.commandTickManipulate))
                .executes((c) -> step(1))
                .then(argument("ticks", integer(1,72000))
                    .suggests((c, b) -> suggestMatching(new String[]{"20"},b))
                    .executes(c -> step(getInteger(c, "ticks")))))
            .then(literal("health")
                    .executes(c -> healthReport(100))
                    .then(argument("ticks", integer(20, 24000))
                            .executes(c -> healthReport(getInteger(c, "ticks")))))
            .then(createReportCommand("entities", TickCommand::healthEntities))
            .then(literal("measure")
                .executes(c -> measureCurrent(c.getSource()))
                .then(argument("ticks", integer(10, 24000))
                    .executes(c -> measure(c.getSource(), getInteger(c, "ticks")))));
        dispatcher.register(tick);
    }

    private static LiteralArgumentBuilder<ServerCommandSource> createReportCommand(String name, ToIntBiFunction<Integer, ReportBoundingBox> schedule) {
        return literal(name)
            .executes(c -> schedule.applyAsInt(100, null))
            .then(argument("ticks", integer(20, 24000))
                .executes(c -> schedule.applyAsInt(getInteger(c, "ticks"), null)))
            .then(argument("from", blockPos())
                .then(argument("to", blockPos())
                    .executes(c -> schedule.applyAsInt(100, getBoundingBox(c)))
                    .then(argument("ticks", integer(20, 24000))
                        .executes(c -> schedule.applyAsInt(getInteger(c, "ticks"), getBoundingBox(c))))));
    }

    private static ReportBoundingBox getBoundingBox(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return new ReportBoundingBox(ctx.getSource().getWorld().getRegistryKey(), BlockBox.create(getBlockPos(ctx, "from"), getBlockPos(ctx, "to")));
    }

    private static int setTps(ServerCommandSource source, float tps) {
        TickSpeed.getServerTickSpeed().setTickRateGoal(tps);
        sendCurrentTPS(source);
        return (int) tps;
    }

    private static int sendCurrentTPS(ServerCommandSource source) {
        float tickRateGoal = TickSpeed.getServerTickSpeed().tickRateGoal;
        m(source, t(Keys.CURRENT, formats("%.1f", Formatting.BOLD, tickRateGoal)));
        return (int) tickRateGoal;
    }

    private static int setWarp(ServerCommandSource source, int advance, String tailCommand) {
        Text message = TickSpeed.getServerTickSpeed().setTickWarp(source, advance, tailCommand);
        if (message != null) m(source, message);
        return advance;
    }

    private static int displayStatus(ServerCommandSource source) {
        TickSpeed tickSpeed = TickSpeed.getServerTickSpeed();
        long warpTotal = tickSpeed.getWarpTimeTotal();
        if (warpTotal == 0) {
            m(source, WARP_STATUS_INACTIVE);
            return 0;
        }
        long warpRemaining = tickSpeed.getWarpTimeRemaining();
        long warpDone = warpTotal - warpRemaining;
        double percentDone = Math.round(warpDone * 1000.0 / warpTotal) / 10.0;
        m(source, ts(Keys.WARP_STATUS_ACTIVE, Formatting.DARK_GREEN, warpDone, warpTotal, percentDone));
        ServerCommandSource sender = tickSpeed.getTickWarpSender();
        if (sender != null) m(source, t(Keys.WARP_STATUS_STARTED_BY, sender.getDisplayName()));
        String callback = tickSpeed.getTickWarpCallback();
        if (callback != null) m(source, t(Keys.WARP_STATUS_CALLBACK, callback));
        return warpRemaining > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) warpRemaining;
    }

    private static int step(int ticks) {
        TickSpeed.getServerTickSpeed().setStep(ticks);
        return 1;
    }

    private static int toggleFreeze(ServerCommandSource source) {
        TickSpeed tickSpeed = TickSpeed.getServerTickSpeed();
        tickSpeed.setPaused(!tickSpeed.isPaused());
        m(source, tickSpeed.isPaused() ? FREEZE : UNFREEZE);
        return 1;
    }

    private static int healthReport(int ticks) {
        CarpetProfiler.scheduleHealthReport(ticks);
        return 1;
    }

    private static int healthEntities(int ticks, @Nullable ReportBoundingBox bbox) {
        CarpetProfiler.scheduleEntitiesReport(ticks, bbox);
        return 1;
    }

    private static int measureCurrent(ServerCommandSource source) {
        printMSPTStats(source, TickSpeed.getMSPTStats(source.getServer()));
        return 1;
    }

    private static int measure(ServerCommandSource source, int ticks) {
        TickSpeed.startMeasurement(source, ticks);
        return 1;
    }

    public static void printMSPTStats(ServerCommandSource source, TickSpeed.MSPTStatistics stats) {
        m(source, ts(Keys.STATS, Formatting.DARK_GREEN, s(Integer.toString(stats.count), Formatting.AQUA)), s(":", Formatting.GRAY));
        m(source, STATS_LOADAVG, s(": ", Formatting.GRAY),
            formats("%.3f", Formatting.AQUA, TickSpeed.getExponential1MinuteMSPT()), s(", ", Formatting.GRAY),
            formats("%.3f", Formatting.AQUA, TickSpeed.getExponential5MinuteMSPT()), s(", ", Formatting.GRAY),
            formats("%.3f", Formatting.AQUA, TickSpeed.getExponential15MinuteMSPT())
        );
        m(source, STATS_MINAVGMAX, s(": ", Formatting.GRAY),
            formats("%.3f", Formatting.AQUA, stats.min), s(", ", Formatting.GRAY),
            formats("%.3f±%.3f", Formatting.AQUA, stats.mean, stats.stdDev), s(", ", Formatting.GRAY),
            formats("%.3f", Formatting.AQUA, stats.max)
        );
        m(source, STATS_LAGTICKS, s(": ", Formatting.GRAY),
            formats("%.1f%%", Formatting.AQUA, stats.lagPercentage)
        );
        m(source, STATS_PERCENTILES, s(": ", Formatting.GRAY),
            formats("%.3f", Formatting.AQUA, stats.percentile90), s(", ", Formatting.GRAY),
            formats("%.3f", Formatting.AQUA, stats.percentile95), s(", ", Formatting.GRAY),
            formats("%.3f", Formatting.AQUA, stats.percentile99)
        );
    }
}
