package quickcarpet.helper;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import quickcarpet.QuickCarpet;
import quickcarpet.commands.TickCommand;
import quickcarpet.logging.Logger;
import quickcarpet.pubsub.PubSubInfoProvider;
import quickcarpet.utils.Messenger;

import java.util.*;

public class TickSpeed {
    public static float tickRateGoal = 20.0f;
    public static long msptGoal = 50;
    private static long timeBias = 0;
    public static long tickWarpStartTime = 0;
    private static long tickWarpScheduledTicks = 0;
    private static int stepAmount = 0;
    public static boolean paused = false;
    private static String tickWarpCallback = null;
    private static ServerCommandSource tickWarpSender = null;

    static {
        new PubSubInfoProvider<>(QuickCarpet.PUBSUB, "minecraft.performance.mspt", 20, TickSpeed::getCurrentMSPT);
        new PubSubInfoProvider<>(QuickCarpet.PUBSUB, "minecraft.performance.tps", 20, TickSpeed::getTPS);
    }

    public static void setTickRateGoal(float rate) {
        tickRateGoal = rate;
        msptGoal = (long) (1000.0 / tickRateGoal);
        if (msptGoal <= 0) {
            msptGoal = 1;
            tickRateGoal = 1000.0f;
        }
    }

    public static void setStep(int ticks) {
        if (ticks <= 0) throw new IllegalArgumentException("Step amount must be positive");
        paused = false;
        stepAmount = ticks + 1;
    }

    public static Text setTickWarp(PlayerEntity player, int warpAmount, String callback, ServerCommandSource source) {
        if (0 == warpAmount) {
            tickWarpCallback = null;
            tickWarpSender = null;
            finishTickWarp();
            return Messenger.c("gi Warp interrupted");
        }
        if (timeBias > 0) {
            return Messenger.c("l Another player is already advancing time at the moment. Try later or talk to them");
        }
        tickWarpStartTime = System.nanoTime();
        tickWarpScheduledTicks = warpAmount;
        timeBias = warpAmount;
        tickWarpCallback = callback;
        tickWarpSender = source;
        return Messenger.c("gi Warp speed ....");
    }

    private static void finishTickWarp() {
        long completed_ticks = tickWarpScheduledTicks - timeBias;
        double milis_to_complete = System.nanoTime() - tickWarpStartTime;
        if (milis_to_complete == 0.0) {
            milis_to_complete = 1.0;
        }
        milis_to_complete /= 1000000.0;
        int tps = (int) (1000.0D * completed_ticks / milis_to_complete);
        double mspt = (1.0 * milis_to_complete) / completed_ticks;
        tickWarpScheduledTicks = 0;
        tickWarpStartTime = 0;
        if (tickWarpCallback != null) {
            CommandManager icommandmanager = tickWarpSender.getMinecraftServer().getCommandManager();
            try {
                int j = icommandmanager.execute(tickWarpSender, tickWarpCallback);

                if (j < 1) {
                    if (tickWarpSender != null) {
                        Messenger.m(tickWarpSender, "r Command Callback failed: ", "rb /" + tickWarpCallback, "/" + tickWarpCallback);
                    }
                }
            } catch (Throwable var23) {
                if (tickWarpSender != null) {
                    Messenger.m(tickWarpSender, "r Command Callback failed - unknown error: ", "rb /" + tickWarpCallback, "/" + tickWarpCallback);
                }
            }
            tickWarpCallback = null;
        }
        if (tickWarpSender != null) {
            Messenger.m(tickWarpSender, String.format("gi ... Time warp completed with %d tps, or %.2f mspt", tps, mspt));
            tickWarpSender = null;
        } else {
            Messenger.print_server_message(QuickCarpet.minecraft_server, String.format("... Time warp completed with %d tps, or %.2f mspt", tps, mspt));
        }
        timeBias = 0;

    }

    public static boolean continueWarp() {
        if (timeBias > 0) {
            if (timeBias == tickWarpScheduledTicks) { //first call after previous tick, adjust start time
                tickWarpStartTime = System.nanoTime();
            }
            timeBias -= 1;
            return true;
        } else {
            finishTickWarp();
            return false;
        }
    }

    public static void tick(MinecraftServer server) {
        int ticks = server.getTicks();
        if (ticks > 100 && ticks % 100 == 0) { // ignore spike at server start
            updateLoadAvg();
        }
        Measurement.tickAll();
        if (stepAmount > 0) {
            stepAmount--;
            if (stepAmount == 0) paused = true;
        }
    }

    public static double getCurrentMSPT() {
        return MathHelper.average(QuickCarpet.minecraft_server.lastTickLengths) * 1.0E-6D;
    }

    public static boolean resetLoadAvg = true;
    private static double loadAvg1min;
    private static final double exp1min = 1 / Math.exp(5. / (1 * 60.));
    private static double loadAvg5min;
    private static final double exp5min = 1 / Math.exp(5. / (5 * 60.));
    private static double loadAvg15min;
    private static final double exp15min = 1 / Math.exp(5. / (15 * 60.));

    private static void updateLoadAvg() {
        double currentLoad = getCurrentMSPT();
        if (resetLoadAvg) {
            loadAvg1min = currentLoad;
            loadAvg5min = currentLoad;
            loadAvg15min = currentLoad;
            resetLoadAvg = false;
            return;
        }
        loadAvg1min = loadAvg1min * exp1min + currentLoad * (1 - exp1min);
        loadAvg5min = loadAvg1min * exp5min + currentLoad * (1 - exp5min);
        loadAvg15min = loadAvg1min * exp15min + currentLoad * (1 - exp15min);
    }

    public static MSPTStatistics getMSPTStats() {
        return new MSPTStatistics(QuickCarpet.minecraft_server.lastTickLengths);
    }

    public static class MSPTStatistics {
        public final int count;
        public final double min;
        public final double max;
        public final double mean;
        public final double variance;
        public final double stdDev;
        public final double lagPercentage;
        public final double percentile90;
        public final double percentile95;
        public final double percentile99;

        private MSPTStatistics(long[] lastTickLengths) {
            this.count = lastTickLengths.length;
            long min = Long.MAX_VALUE;
            long max = 0;
            long total = 0;
            int lagTicks = 0;
            for (long tick : lastTickLengths) {
                if (tick < min) min = tick;
                else if (tick > max) max = tick;
                total += tick;
                if (tick > 50000000) lagTicks++;
            }
            double mean = (double) total / lastTickLengths.length;
            double variance = 0;
            for (long lastTickLength : lastTickLengths) {
                double r = lastTickLength - mean;
                variance += r * r;
            }
            variance /= lastTickLengths.length;
            this.min = min / 1e6;
            this.max = max / 1e6;
            this.mean = mean / 1e6;
            this.variance = variance / 1e6;
            this.stdDev = Math.sqrt(variance) / 1e6;
            this.lagPercentage = 100. * lagTicks / lastTickLengths.length;
            long[] sorted = Arrays.copyOf(lastTickLengths, lastTickLengths.length);
            Arrays.sort(sorted);
            this.percentile90 = sorted[(90 * sorted.length) / 100 - 1] / 1e6;
            this.percentile95 = sorted[(95 * sorted.length) / 100 - 1] / 1e6;
            this.percentile99 = sorted[(99 * sorted.length) / 100 - 1] / 1e6;
        }

        @Override
        public String toString() {
            return String.format("min=%.3f, max=%.3f, avg=%.3f, stdDev=%.3f, >50=%.3f%%, 90%%=%.3f, 95%%=%.3f, 99%%=%.3f",
                    min, max, mean, stdDev, lagPercentage, percentile90, percentile95, percentile99);
        }
    }

    private static class Measurement {
        private static Map<ServerCommandSource, Measurement> measurements = new HashMap<>();
        public final ServerCommandSource source;
        public final int length;
        public final long[] tickLengths;
        private int ticksRecorded;

        private Measurement(ServerCommandSource source, int length) {
            this.source = source;
            this.length = length;
            this.tickLengths = new long[length];
            measurements.put(source, this);
        }

        private void tick() {
            if (ticksRecorded >= length) {
                TickCommand.printMSPTStats(source, new MSPTStatistics(tickLengths));
                measurements.remove(source);
                return;
            }
            int previous = (QuickCarpet.minecraft_server.getTicks() - 1) % 100;
            tickLengths[ticksRecorded++] = QuickCarpet.minecraft_server.lastTickLengths[previous];
        }

        private static void tickAll() {
            for (Measurement m : measurements.values()) m.tick();
        }
    }

    public static void startMeasurement(ServerCommandSource source, int ticks) {
        if (Measurement.measurements.containsKey(source)) return;
        new Measurement(source, ticks);
    }

    public static double getExponential1MinuteMSPT() {
        return loadAvg1min;
    }

    public static double getExponential5MinuteMSPT() {
        return loadAvg5min;
    }

    public static double getExponential15MinuteMSPT() {
        return loadAvg15min;
    }

    public static double calculateTPS(double mspt) {
        return 1000.0D / Math.max((TickSpeed.tickWarpStartTime != 0) ? 0.0 : TickSpeed.msptGoal, mspt);
    }

    public static double getTPS() {
        return calculateTPS(getCurrentMSPT());
    }

    public static class LogCommandParameters extends AbstractMap<String, Object> implements Logger.CommandParameters {
        public static final LogCommandParameters INSTANCE = new LogCommandParameters();
        private LogCommandParameters() {}
        @Override
        public Set<Entry<String, Object>> entrySet() {
            Map<String, Object> counts = new LinkedHashMap<>();
            double mspt = getCurrentMSPT();
            counts.put("MSPT", mspt);
            counts.put("TPS", calculateTPS(mspt));
            return counts.entrySet();
        }
    }
}
