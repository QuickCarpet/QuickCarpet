package quickcarpet.helper;

import net.minecraft.ChatFormat;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.MathHelper;
import quickcarpet.QuickCarpet;
import quickcarpet.commands.TickCommand;
import quickcarpet.pubsub.PubSubInfoProvider;
import quickcarpet.utils.Messenger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TickSpeed {
    
    public static final int PLAYER_GRACE = 2;
    public static float tickrate = 20.0f;
    public static long mspt = 50l;
    public static long time_bias = 0;
    public static long time_warp_start_time = 0;
    public static long time_warp_scheduled_ticks = 0;
    public static PlayerEntity time_advancerer = null;
    public static String tick_warp_callback = null;
    public static ServerCommandSource tick_warp_sender = null;
    public static int player_active_timeout = 0;
    public static boolean process_entities = true;
    public static boolean is_paused = false;
    public static boolean is_superHot = false;
    
    static {
        new PubSubInfoProvider<>(QuickCarpet.PUBSUB, "minecraft.performance.mspt", 20, TickSpeed::getCurrentMSPT);
        new PubSubInfoProvider<>(QuickCarpet.PUBSUB, "minecraft.performance.tps", 20, TickSpeed::getTPS);
    }
    
    public static void reset_player_active_timeout()
    {
        if (player_active_timeout < PLAYER_GRACE)
        {
            player_active_timeout = PLAYER_GRACE;
        }
    }
    
    public static void add_ticks_to_run_in_pause(int ticks)
    {
        player_active_timeout = PLAYER_GRACE+ticks;
    }
    
    public static void tickrate(float rate)
    {
        tickrate = rate;
        mspt = (long)(1000.0/tickrate);
        if (mspt <=0)
        {
            mspt = 1l;
            tickrate = 1000.0f;
        }
    }
    
    public static Component tickrate_advance(PlayerEntity player, int advance, String callback, ServerCommandSource source)
    {
        if (0 == advance)
        {
            tick_warp_callback = null;
            tick_warp_sender = null;
            finish_time_warp();
            return Messenger.c("gi Warp interrupted");
        }
        if (time_bias > 0)
        {
            return Messenger.c("l Another player is already advancing time at the moment. Try later or talk to them");
        }
        time_advancerer = player;
        time_warp_start_time = System.nanoTime();
        time_warp_scheduled_ticks = advance;
        time_bias = advance;
        tick_warp_callback = callback;
        tick_warp_sender = source;
        return Messenger.c("gi Warp speed ....");
    }
    
    public static void finish_time_warp()
    {
        
        long completed_ticks = time_warp_scheduled_ticks - time_bias;
        double milis_to_complete = System.nanoTime()-time_warp_start_time;
        if (milis_to_complete == 0.0)
        {
            milis_to_complete = 1.0;
        }
        milis_to_complete /= 1000000.0;
        int tps = (int) (1000.0D*completed_ticks/milis_to_complete);
        double mspt = (1.0*milis_to_complete)/completed_ticks;
        time_warp_scheduled_ticks = 0;
        time_warp_start_time = 0;
        if (tick_warp_callback != null)
        {
            CommandManager icommandmanager = tick_warp_sender.getMinecraftServer().getCommandManager();
            try
            {
                int j = icommandmanager.execute(tick_warp_sender, tick_warp_callback);
                
                if (j < 1)
                {
                    if (time_advancerer != null)
                    {
                        Messenger.m(time_advancerer, "r Command Callback failed: ", "rb /"+tick_warp_callback,"/"+tick_warp_callback);
                    }
                }
            }
            catch (Throwable var23)
            {
                if (time_advancerer != null)
                {
                    Messenger.m(time_advancerer, "r Command Callback failed - unknown error: ", "rb /"+tick_warp_callback,"/"+tick_warp_callback);
                }
            }
            tick_warp_callback = null;
            tick_warp_sender = null;
        }
        if (time_advancerer != null)
        {
            Messenger.m(time_advancerer, String.format("gi ... Time warp completed with %d tps, or %.2f mspt",tps, mspt ));
            time_advancerer = null;
        }
        else
        {
            Messenger.print_server_message(QuickCarpet.minecraft_server, String.format("... Time warp completed with %d tps, or %.2f mspt",tps, mspt ));
        }
        time_bias = 0;
        
    }
    
    public static boolean continueWarp()
    {
        if (time_bias > 0)
        {
            if (time_bias == time_warp_scheduled_ticks) //first call after previous tick, adjust start time
            {
                time_warp_start_time = System.nanoTime();
            }
            time_bias -= 1;
            return true;
        }
        else
        {
            finish_time_warp();
            return false;
        }
    }
    
    public static void tick(MinecraftServer server)
    {
        process_entities = true;
        if (player_active_timeout > 0)
        {
            player_active_timeout--;
        }
        if (is_paused)
        {
            if (player_active_timeout < PLAYER_GRACE)
            {
                process_entities = false;
            }
        }
        else if (is_superHot)
        {
            if (player_active_timeout <= 0)
            {
                process_entities = false;
                
            }
        }
        int ticks = server.getTicks();
        if (ticks > 100 && ticks % 100 == 0) { // ignore spike at server start
            updateLoadAvg();
        }
        Measurement.tickAll();
    }
    
    
    // EDD's Tick warping stuff
    private static final String SS = "\u00A7", RED = SS + 'c', GREEN = SS + 'a', RESET = SS + 'r', CYAN =  SS + 'b';
    
    public static boolean isWarping;
    public static float serverTPS = 20.0F;
    
    public static long ms_per_tick = (long)(1000 / serverTPS),
            warn_time = (long)(serverTPS * 200),
            last_sleep = 0,
            elapsed_ticks = 0,
            ticksToWarp = 0,
            warpedTicks = 0,
            warpStartMs = 0;
    
    public static int sendUsage(ServerCommandSource source)
    {
        return send(source, "Server TPS = " + GREEN + serverTPS);
    }
    
    public static void updateTPS()
    {
        ms_per_tick = (long)(1000 / serverTPS);
        warn_time = (long)(serverTPS * 200);
    }
    
    public static int send(ServerCommandSource source, String s)
    {
        source.sendFeedback(new TranslatableComponent("" + s), true);
        return 1;
    }
    
    public static int setWarp(ServerCommandSource source, int ticks)
    {
        if (ticks <= 0)
        {
            ticksToWarp = 0;
            return 1;
        }
        
        warpStartMs = System.currentTimeMillis();
        warpedTicks = 0;
        ticksToWarp = ticks;
        ms_per_tick = 1;
        isWarping = true;
        return send(source, "Warping " + GREEN + ticks + RESET + " ticks...");
    }
    
    public static void processWarp(MinecraftServer server)
    {
        if (isWarping)
        {
            --ticksToWarp;
            ++warpedTicks;
            
            if (ticksToWarp <= 0)
            {
                ticksToWarp = 0;
                isWarping = false;
                long msDiff = System.currentTimeMillis() - warpStartMs;
                float secs = msDiff / 1000F;
                float mspt = msDiff / (float)warpedTicks;
                int tps = (int) (1000 / mspt);
                //server.getPlayerManager().sendToAll(new TranslatableComponent(String.format('[' + CYAN + "TPS" + RESET + "] Done in %s%.2f%ss (%s%.2f%smspt)", GREEN, secs, RESET, GREEN, mspt, RESET)));
                server.getPlayerManager().sendToAll(new TranslatableComponent("... Time warp completed with " + ChatFormat.AQUA + tps + " tps" + ChatFormat.RESET + ", or " + ChatFormat.GREEN + mspt + " mspt"));
                updateTPS();
            }
        }
    }
    
    public static int setRate(ServerCommandSource source, float tps)
    {
        serverTPS = tps <= 0 ? 0.1F : tps;
        updateTPS();
        return send(source, "Server TPS = " + GREEN + serverTPS);
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
        return 1000.0D / Math.max((TickSpeed.time_warp_start_time != 0)?0.0:TickSpeed.mspt, mspt);
    }

    public static double getTPS() {
        return calculateTPS(getCurrentMSPT());
    }
}
