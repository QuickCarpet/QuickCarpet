package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import quickcarpet.QuickCarpet;
import quickcarpet.helper.TickSpeed;
import quickcarpet.settings.Settings;
import quickcarpet.utils.CarpetProfiler;

import static com.mojang.brigadier.arguments.FloatArgumentType.floatArg;
import static com.mojang.brigadier.arguments.FloatArgumentType.getFloat;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandSource.suggestMatching;
import static quickcarpet.utils.Messenger.*;

public class TickCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> tick = literal("tick")
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
                .executes(c -> healthReport(c.getSource(), 100))
                .then(argument("ticks", integer(20, 24000))
                    .executes(c -> healthReport(c.getSource(), getInteger(c, "ticks")))))
            .then(literal("entities")
                .executes(c -> healthEntities(c.getSource(), 100))
                .then(argument("ticks", integer(20, 24000))
                    .executes(c -> healthEntities(c.getSource(), getInteger(c, "ticks")))))
            .then(literal("measure")
                .executes(c -> measureCurrent(c.getSource()))
                .then(argument("ticks", integer(10, 24000))
                    .executes(c -> measure(c.getSource(), getInteger(c, "ticks")))));
        dispatcher.register(tick);
    }

    private static int setTps(ServerCommandSource source, float tps) {
        QuickCarpet.getInstance().tickSpeed.setTickRateGoal(tps);
        sendCurrentTPS(source);
        return (int) tps;
    }

    private static int sendCurrentTPS(ServerCommandSource source) {
        float tickRateGoal = QuickCarpet.getInstance().tickSpeed.tickRateGoal;
        m(source, t("command.tick.current", formats("%.1f", BOLD, tickRateGoal)));
        return (int) tickRateGoal;
    }

    private static int setWarp(ServerCommandSource source, int advance, String tailCommand) {
        Text message = QuickCarpet.getInstance().tickSpeed.setTickWarp(source, advance, tailCommand);
        if (message != null) m(source, message);
        return advance;
    }

    private static int displayStatus(ServerCommandSource source) {
        TickSpeed tickSpeed = QuickCarpet.getInstance().tickSpeed;
        long warpTotal = tickSpeed.getWarpTimeTotal();
        if (warpTotal == 0) {
            m(source, ts("command.tick.warp.status.inactive", YELLOW));
            return 0;
        }
        long warpRemaining = tickSpeed.getWarpTimeRemaining();
        long warpDone = warpTotal - warpRemaining;
        double percentDone = Math.round(warpDone * 1000.0 / warpTotal) / 10.0;
        m(source, ts("command.tick.warp.status.active", DARK_GREEN, warpDone, warpTotal, percentDone));
        ServerCommandSource sender = tickSpeed.getTickWarpSender();
        if (sender != null) m(source, t("command.tick.warp.status.startedBy", sender.getDisplayName()));
        String callback = tickSpeed.getTickWarpCallback();
        if (callback != null) m(source, t("command.tick.warp.status.callback", callback));
        return warpRemaining > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) warpRemaining;
    }

    private static int step(int ticks) {
        QuickCarpet.getInstance().tickSpeed.setStep(ticks);
        return 1;
    }

    private static int toggleFreeze(ServerCommandSource source) {
        TickSpeed tickSpeed = QuickCarpet.getInstance().tickSpeed;
        tickSpeed.setPaused(!tickSpeed.isPaused());
        if (tickSpeed.isPaused()) {
            m(source, ts("command.tick.freeze", GRAY + "" + ITALIC));
        } else {
            m(source, ts("command.tick.unfreeze", GRAY + "" + ITALIC));
        }
        return 1;
    }

    private static int healthReport(CommandSource source, int ticks) {
        CarpetProfiler.startTickReport(CarpetProfiler.ReportType.HEALTH, ticks);
        return 1;
    }

    private static int healthEntities(CommandSource source, int ticks) {
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
        m(source, ts("command.tick.stats", DARK_GREEN, s(Integer.toString(stats.count), CYAN)), s(":", GRAY));
        m(source, t("command.tick.stats.loadavg"), s(": ", GRAY),
            formats("%.3f", CYAN, TickSpeed.getExponential1MinuteMSPT()), s(", ", GRAY),
            formats("%.3f", CYAN, TickSpeed.getExponential5MinuteMSPT()), s(", ", GRAY),
            formats("%.3f", CYAN, TickSpeed.getExponential15MinuteMSPT())
        );
        m(source, t("command.tick.stats.minavgmax"), s(": ", GRAY),
            formats("%.3f", CYAN, stats.min), s(", ", GRAY),
            formats("%.3f±%.3f", CYAN, stats.mean, stats.stdDev), s(", ", GRAY),
            formats("%.3f", CYAN, stats.max)
        );
        m(source, t("command.tick.stats.lagticks"), s(": ", GRAY),
            formats("%.1f%%", CYAN, stats.lagPercentage)
        );
        m(source, t("command.tick.stats.percentiles"), s(": ", GRAY),
            formats("%.3f", CYAN, stats.percentile90), s(", ", GRAY),
            formats("%.3f", CYAN, stats.percentile95), s(", ", GRAY),
            formats("%.3f", CYAN, stats.percentile99)
        );
    }
}
