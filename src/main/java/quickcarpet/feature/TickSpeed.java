package quickcarpet.feature;

import com.google.gson.JsonObject;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import quickcarpet.QuickCarpet;
import quickcarpet.QuickCarpetServer;
import quickcarpet.api.TelemetryProvider;
import quickcarpet.commands.TickCommand;
import quickcarpet.pubsub.PubSubInfoProvider;
import quickcarpet.utils.Constants.TickCommand.Keys;
import quickcarpet.utils.Messenger;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static quickcarpet.utils.Constants.TickCommand.Texts.*;
import static quickcarpet.utils.Messenger.*;

public class TickSpeed implements TelemetryProvider {
    public final boolean isClient;
    private final MinecraftServer server;
    public float tickRateGoal = 20;
    public float msptGoal = 50;
    private long warpTimeRemaining = 0;
    public long tickWarpStartTime = 0;
    private long tickWarpScheduledTicks = 0;
    private int stepAmount = 0;
    private boolean paused = false;
    private String tickWarpCallback = null;
    private ServerCommandSource tickWarpSender = null;

    private static final PubSubInfoProvider<Float> TICK_RATE_GOAL_PUBSUB_PROVIDER = new PubSubInfoProvider<>(QuickCarpet.PUBSUB, "carpet.tick-rate.tps-goal", 0, () -> getServerTickSpeed().tickRateGoal);
    private static final PubSubInfoProvider<Integer> TICK_STEP_PUBSUB_PROVIDER = new PubSubInfoProvider<>(QuickCarpet.PUBSUB, "carpet.tick-rate.step", 0, () -> getServerTickSpeed().stepAmount);
    private static final PubSubInfoProvider<Boolean> PAUSED_PUBSUB_PROVIDER = new PubSubInfoProvider<>(QuickCarpet.PUBSUB, "carpet.tick-rate.paused", 0, () -> getServerTickSpeed().paused);

    static {
        new PubSubInfoProvider<>(QuickCarpet.PUBSUB, "minecraft.performance.mspt", 20, () -> getServerTickSpeed().getCurrentMSPT());
        new PubSubInfoProvider<>(QuickCarpet.PUBSUB, "minecraft.performance.tps", 20, () -> getServerTickSpeed().getTPS());
    }

    public TickSpeed(@Nullable MinecraftServer server) {
        this.isClient = server == null;
        this.server = server;
    }

    public static TickSpeed getServerTickSpeed() {
        return QuickCarpetServer.getInstance().tickSpeed;
    }

    public void setTickRateGoal(float rate) {
        tickRateGoal = rate;
        msptGoal = 1000f / rate;
        if (msptGoal <= 0) {
            msptGoal = 1;
            tickRateGoal = 1000.0f;
        }
        if (!isClient) TICK_RATE_GOAL_PUBSUB_PROVIDER.publish();
    }

    public void setStep(int ticks) {
        if (ticks <= 0) throw new IllegalArgumentException("Step amount must be positive");
        paused = false;
        stepAmount = ticks + 1;
        if (!isClient) TICK_STEP_PUBSUB_PROVIDER.publish();
    }

    public void setStepAmount(int amount) {
        if (amount <= 0) return;
        paused = false;
        stepAmount = amount;
    }

    public Text setTickWarp(@Nullable ServerCommandSource source, int warpAmount, @Nullable String callback) {
        if (0 == warpAmount) {
            tickWarpCallback = null;
            tickWarpSender = null;
            finishTickWarp();
            return WARP_INTERRUPTED;
        }
        if (warpTimeRemaining > 0) return WARP_ACTIVE;
        tickWarpStartTime = System.nanoTime();
        tickWarpScheduledTicks = warpAmount;
        warpTimeRemaining = warpAmount;
        tickWarpCallback = callback;
        tickWarpSender = source;
        return WARP_START;
    }

    public long getWarpTimeTotal() {
        return tickWarpScheduledTicks;
    }

    public long getWarpTimeRemaining() {
        return warpTimeRemaining;
    }

    @Nullable
    public ServerCommandSource getTickWarpSender() {
        return tickWarpSender;
    }

    @Nullable
    public String getTickWarpCallback() {
        return tickWarpCallback;
    }

    private void finishTickWarp() {
        long completedTicks = tickWarpScheduledTicks - warpTimeRemaining;
        double timeRunning = System.nanoTime() - tickWarpStartTime;
        if (timeRunning == 0.0) {
            timeRunning = 1.0;
        }
        timeRunning /= 1000000.0;
        int tps = (int) (1000.0D * completedTicks / timeRunning);
        double mspt = timeRunning / completedTicks;
        tickWarpScheduledTicks = 0;
        tickWarpStartTime = 0;
        MinecraftServer server = QuickCarpetServer.getMinecraftServer();
        if (tickWarpCallback != null) {
            CommandManager cmdManager = server.getCommandManager();
            try {
                int j = cmdManager.executeWithPrefix(tickWarpSender, tickWarpCallback);

                if (j < 1) {
                    if (tickWarpSender != null) {
                        m(tickWarpSender, ts(Keys.WARP_CALLBACK_FAILED, Formatting.RED, runCommand(s("/" + tickWarpCallback, Formatting.UNDERLINE), "/" + tickWarpCallback)));
                    }
                }
            } catch (Throwable t) {
                if (tickWarpSender != null) {
                    m(tickWarpSender, ts(Keys.WARP_CALLBACK_FAILED_UNKNOWN, Formatting.RED, runCommand(s("/" + tickWarpCallback, Formatting.UNDERLINE), "/" + tickWarpCallback)));
                }
            }
            tickWarpCallback = null;
        }
        MutableText message = ts(Keys.WARP_COMPLETED, GRAY_ITALIC, tps, String.format("%.2f", mspt));
        if (tickWarpSender != null) {
            m(tickWarpSender, message);
            tickWarpSender = null;
        } else {
            Messenger.broadcast(server, message);
        }
        warpTimeRemaining = 0;

    }

    public boolean continueWarp() {
        if (warpTimeRemaining > 0) {
            if (warpTimeRemaining == tickWarpScheduledTicks) { //first call after previous tick, adjust start time
                tickWarpStartTime = System.nanoTime();
            }
            warpTimeRemaining -= 1;
            return true;
        } else {
            finishTickWarp();
            return false;
        }
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        if (!isClient) PAUSED_PUBSUB_PROVIDER.publish();
    }

    public boolean isPaused() {
        return paused;
    }

    public void tick() {
        if (server != null) {
            int ticks = server.getTicks();
            if (ticks > 100 && ticks % 100 == 0) { // ignore spike at server start
                updateLoadAvg();
            }
            Measurement.tickAll();
        }
        if (stepAmount > 0) {
            stepAmount--;
            if (stepAmount == 0) {
                if (!isClient) TICK_STEP_PUBSUB_PROVIDER.publish();
                setPaused(true);
            }
        }
    }

    public double getCurrentMSPT() {
        return server == null ? 0 : Arrays.stream(server.lastTickLengths).average().getAsDouble() * 1.0E-6D;
    }

    public static boolean resetLoadAvg = true;
    private static double loadAvg1min;
    private static final double exp1min = 1 / Math.exp(5. / (1 * 60.));
    private static double loadAvg5min;
    private static final double exp5min = 1 / Math.exp(5. / (5 * 60.));
    private static double loadAvg15min;
    private static final double exp15min = 1 / Math.exp(5. / (15 * 60.));

    private void updateLoadAvg() {
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

    public static MSPTStatistics getMSPTStats(MinecraftServer server) {
        return new MSPTStatistics(server.lastTickLengths);
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
        private static final Map<ServerCommandSource, Measurement> MEASUREMENTS = new HashMap<>();
        public final ServerCommandSource source;
        private final MinecraftServer server;
        public final int length;
        public final long[] tickLengths;
        private int ticksRecorded;

        private Measurement(ServerCommandSource source, int length) {
            this.source = source;
            this.server = source.getServer();
            this.length = length;
            this.tickLengths = new long[length];
            MEASUREMENTS.put(source, this);
        }

        private void tick() {
            if (ticksRecorded >= length) {
                TickCommand.printMSPTStats(source, new MSPTStatistics(tickLengths));
                MEASUREMENTS.remove(source);
                return;
            }
            int previous = (server.getTicks() - 1) % 100;
            tickLengths[ticksRecorded++] = server.lastTickLengths[previous];
        }

        private static void tickAll() {
            for (Measurement m : MEASUREMENTS.values()) m.tick();
        }
    }

    public static void startMeasurement(ServerCommandSource source, int ticks) {
        if (Measurement.MEASUREMENTS.containsKey(source)) return;
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

    public double calculateTPS(double mspt) {
        return 1000.0D / Math.max((tickWarpStartTime != 0) ? 0.0 : msptGoal, mspt);
    }

    public double getTPS() {
        return calculateTPS(getCurrentMSPT());
    }

    @Override
    public JsonObject getTelemetryData() {
        JsonObject obj = new JsonObject();
        double mspt = getCurrentMSPT();
        obj.addProperty("tps", calculateTPS(mspt));
        obj.addProperty("mspt", mspt);
        obj.addProperty("msptGoal", msptGoal);
        obj.addProperty("paused", paused);
        obj.addProperty("warpTimeRemaining", warpTimeRemaining);
        JsonObject loadAvg = new JsonObject();
        loadAvg.addProperty("1", loadAvg1min);
        loadAvg.addProperty("5", loadAvg5min);
        loadAvg.addProperty("15", loadAvg15min);
        obj.add("loadAvg", loadAvg);
        return obj;
    }
}
