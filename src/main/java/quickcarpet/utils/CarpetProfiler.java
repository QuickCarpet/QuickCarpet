package quickcarpet.utils;

import com.sun.management.GarbageCollectionNotificationInfo;
import com.sun.management.GcInfo;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import quickcarpet.QuickCarpet;
import quickcarpet.QuickCarpetServer;
import quickcarpet.feature.TickSpeed;
import quickcarpet.logging.LogParameter;
import quickcarpet.logging.Loggers;
import quickcarpet.utils.Constants.OtherKeys;
import quickcarpet.utils.Constants.Profiler.Keys;

import javax.annotation.Nullable;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.openmbean.CompositeData;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.*;

import static quickcarpet.utils.Constants.Profiler.Texts.TOP_10_COUNTS;
import static quickcarpet.utils.Constants.Profiler.Texts.TOP_10_GROSSING;
import static quickcarpet.utils.Messenger.*;

public class CarpetProfiler {
    private static final Logger LOGGER = LogManager.getLogger("QuickCarpet|Profiler");
    @Nullable
    private static Report scheduledReport;
    @Nullable
    private static Report report;
    private static long currentTickStart = 0;
    private static boolean inTick;

    public enum SectionType {
        UNKNOWN(true, false),
        COMMAND_FUNCTIONS(true, false),
        NETWORK(true, false),
        AUTOSAVE(true, true),
        GC(true, true),
        ASYNC_TASKS(true, false, false),
        SPAWNING(false, false),
        BLOCKS(false, false),
        FLUIDS(false, false),
        RANDOM_TICKS(false, false),
        BLOCK_EVENTS(false, false),
        ENTITIES(false, false),
        BLOCK_ENTITIES(false, false),
        RAIDS(false, false),
        PORTALS(false, false),
        ENTITY_MANAGER(false, false),
        CHUNK_UNLOAD(false, false)
        ;

        public static final SectionType[] GLOBAL = Arrays.stream(values()).filter(s -> s.global).toArray(SectionType[]::new);
        public static final SectionType[] PER_DIMENSION = Arrays.stream(values()).filter(s -> !s.global).toArray(SectionType[]::new);

        private final boolean global;
        private final boolean customFormat;
        private final boolean accumulate;
        private final String translationKey = Keys.SECTION_PREFIX + this.name().toLowerCase(Locale.ROOT);

        SectionType(boolean global, boolean custom) {
            this(global, custom, true);
        }

        SectionType(boolean global, boolean custom, boolean accumulate) {
            this.global = global;
            this.customFormat = custom;
            this.accumulate = accumulate;
        }

        public MutableText getName() {
            return t(translationKey);
        }

        public MutableText format(double amount, double avgTime) {
            float msptGoal = TickSpeed.getServerTickSpeed().msptGoal;
            if (customFormat) {
                return t(translationKey + Keys.SECTION_FORMAT_SUFFIX, getName(),
                    formats("%.3f", getHeatmapColor(amount, msptGoal), amount),
                    formats("%.3f", getHeatmapColor(avgTime, msptGoal), avgTime)
                );
            }
            return t(Keys.SECTION_FORMAT, getName(),
                formats("%.3f", getHeatmapColor(amount, msptGoal), amount)
            );
        }
    }

    private static class Measurement {
        final @Nullable RegistryKey<World> dimension;
        final Object2LongMap<SectionType> sections;
        final Object2IntMap<SectionType> sectionCount;
        final Object2LongMap<EntityType<?>> entityTimes = new Object2LongOpenHashMap<>();
        final Object2LongMap<EntityType<?>> entityCount = new Object2LongOpenHashMap<>();
        final Object2LongMap<BlockEntityType<?>> blockEntityTimes = new Object2LongOpenHashMap<>();
        final Object2LongMap<BlockEntityType<?>> blockEntityCount = new Object2LongOpenHashMap<>();

        private SectionType currentSection;
        private long currentSectionStart;

        private EntityType<?> currentEntity;
        private long currentEntityStart;

        private BlockEntityType<?> currentBlockEntity;
        private long currentBlockEntityStart;

        Measurement(@Nullable RegistryKey<World> dimension) {
            this.dimension = dimension;
            if (dimension == null) {
                this.sections = new Object2LongArrayMap<>(SectionType.GLOBAL, new long[SectionType.GLOBAL.length]);
                this.sectionCount = new Object2IntArrayMap<>(SectionType.GLOBAL, new int[SectionType.GLOBAL.length]);
            } else {
                this.sections = new Object2LongArrayMap<>(SectionType.PER_DIMENSION, new long[SectionType.PER_DIMENSION.length]);
                this.sectionCount = new Object2IntArrayMap<>(SectionType.PER_DIMENSION, new int[SectionType.PER_DIMENSION.length]);
            }
        }

        void startSection(SectionType type) {
            if (currentSectionStart != 0 && currentSection != null) {
                LOGGER.info("Section not ended when starting " + type + ": " + currentSection, new Exception());
                endSection(currentSection);
            }
            this.currentSection = type;
            this.currentSectionStart = System.nanoTime();
        }

        void endSection(SectionType type) {
            if (currentSection == null) return;
            if (currentSectionStart == 0) {
                LOGGER.info("Section not started, previous was: " + currentSection + ", expected " + type, new Exception());
                return;
            }
            sections.put(currentSection, sections.getLong(currentSection) + System.nanoTime() - currentSectionStart);
            sectionCount.put(currentSection, sectionCount.getInt(currentSection) + 1);
            currentSectionStart = 0;
        }

        void startEntity(EntityType<?> type) {
            this.currentEntity = type;
            this.currentEntityStart = System.nanoTime();
        }

        void endEntity() {
            if (currentBlockEntityStart == 0) return;
            long previousTime = entityTimes.getOrDefault(currentEntity, 0);
            entityTimes.put(currentEntity, previousTime + System.nanoTime() - currentEntityStart);
            entityCount.put(currentEntity, entityCount.getOrDefault(currentEntity, 0) + 1);
            currentEntityStart = 0;
        }

        void startBlockEntity(BlockEntityType<?> type) {
            this.currentBlockEntity = type;
            this.currentBlockEntityStart = System.nanoTime();
        }

        void endBlockEntity() {
            if (currentBlockEntityStart == 0) return;
            long previousTime = blockEntityTimes.getOrDefault(currentBlockEntity, 0);
            blockEntityTimes.put(currentBlockEntity, previousTime + System.nanoTime() - currentBlockEntityStart);
            blockEntityCount.put(currentBlockEntity, blockEntityCount.getOrDefault(currentBlockEntity, 0) + 1);
            currentBlockEntityStart = 0;
        }

        void gc(long ms) {
            long ns = ms * 1_000_000;
            if (dimension == null) {
                sections.put(SectionType.GC, sections.getLong(SectionType.GC) + ns);
                sectionCount.put(SectionType.GC, sectionCount.getInt(SectionType.GC) + 1);
            }
            if (currentSectionStart != 0) currentSectionStart += ns;
            if (currentBlockEntityStart != 0) currentBlockEntityStart += ns;
            if (currentEntityStart != 0) currentEntityStart += ns;
        }
    }

    private static abstract class Report {
        public final int duration;
        public int ticksRemaining;
        protected boolean started = false;
        protected long totalTickTime;
        protected final Map<RegistryKey<World>, Measurement> measurements = new HashMap<>();

        public Report(int duration) {
            this.duration = duration;
            this.ticksRemaining = duration;
        }

        public void start(MinecraftServer server) {
            this.started = true;
            measurements.put(null, new Measurement(null));
            for (ServerWorld world : server.getWorlds()) {
                measurements.put(world.getRegistryKey(), new Measurement(world.getRegistryKey()));
            }
        }

        public void tick(MinecraftServer server) {
            totalTickTime += System.nanoTime() - currentTickStart;
            if (--ticksRemaining <= 0) {
                report = null;
                finalizeReport(server);
            }
        }

        protected Measurement getMeasurement(World world) {
            if (world == null) return measurements.get(null);
            return measurements.get(world.getRegistryKey());
        }

        abstract void finalizeReport(MinecraftServer server);
    }

    private static class HealthReport extends Report {
        public HealthReport(int ticks) {
            super(ticks);
        }

        public void startSection(World world, SectionType section) {
            getMeasurement(world).startSection(section);
        }

        public void endSection(World world, SectionType section) {
            getMeasurement(world).endSection(section);
        }

        @Override
        protected void finalizeReport(MinecraftServer server) {
            double divider = 1e-6 / duration;
            double avgTickTime = divider * totalTickTime;
            broadcast(server, t(Keys.TITLE, formats("%.3f", getHeatmapColor(avgTickTime, 50), avgTickTime)));
            long accumulated = 0L;

            Measurement global = measurements.get(null);
            for (SectionType section : SectionType.GLOBAL) {
                long nanos = global.sections.getLong(section);
                if (section.accumulate) accumulated += nanos;
                double amount = divider * nanos;
                double avgTime = nanos / (1e6 * global.sectionCount.getInt(section));
                if (amount > 0.01 || avgTime > 0.1) {
                    broadcast(server, section.format(amount, avgTime));
                }
            }
            for (ServerWorld world : QuickCarpetServer.getMinecraftServer().getWorlds()) {
                Measurement measurement = measurements.get(world.getRegistryKey());
                List<MutableText> messages = new ArrayList<>();
                for (SectionType section : SectionType.PER_DIMENSION) {
                    long nanos = measurement.sections.getLong(section);
                    if (section.accumulate) accumulated += nanos;
                    double amount = divider * nanos;
                    double avgTime = nanos / (1e6 * measurement.sectionCount.getInt(section));
                    if (amount > 0.01 || avgTime > 0.1) {
                        messages.add(c(s(" - "), section.format(amount, avgTime)));
                    }
                }
                if (!messages.isEmpty()) {
                    broadcast(server, String.valueOf(world.getRegistryKey().getValue()));
                    for (MutableText m : messages) broadcast(server, m);
                }
            }

            long rest = totalTickTime - accumulated;

            broadcast(server, SectionType.UNKNOWN.format(divider * rest, 0));
        }
    }

    private static class EntitiesReport extends Report {
        private final @Nullable ReportBoundingBox box;

        public EntitiesReport(int ticks, @Nullable ReportBoundingBox box) {
            super(ticks);
            this.box = box;
        }

        public boolean contains(World world, BlockPos pos) {
            return box == null || box.contains(world, pos);
        }

        public void startEntity(World world, EntityType<?> type) {
            getMeasurement(world).startEntity(type);
        }

        public void endEntity(World world) {
            if (box != null && !box.dimension.equals(world.getRegistryKey())) return;
            getMeasurement(world).endEntity();
        }

        public void startBlockEntity(World world, BlockEntityType<?> type) {
            getMeasurement(world).startBlockEntity(type);
        }

        public void endBlockEntity(World world) {
            if (box != null && !box.dimension.equals(world.getRegistryKey())) return;
            getMeasurement(world).endBlockEntity();
        }

        @Override
        protected void finalizeReport(MinecraftServer server) {
            double divider = 1e-6 / duration;
            double avgTickTime = divider * totalTickTime;
            float msptGoal = TickSpeed.getServerTickSpeed().msptGoal;
            broadcast(server, t("carpet.profiler.title", formats("%.3f", getHeatmapColor(avgTickTime, msptGoal), avgTickTime)));
            Object2LongMap<Pair<Measurement, Object>> counts = new Object2LongOpenHashMap<>();
            Object2LongMap<Pair<Measurement, Object>> times = new Object2LongOpenHashMap<>();
            for (Measurement m : measurements.values()) {
                for (EntityType<?> e : m.entityCount.keySet()) {
                    counts.put(new Pair<>(m, e), m.entityCount.getLong(e));
                    times.put(new Pair<>(m, e), m.entityTimes.getLong(e));
                }
                for (BlockEntityType<?> be : m.blockEntityCount.keySet()) {
                    counts.put(new Pair<>(m, be), m.blockEntityCount.getLong(be));
                    times.put(new Pair<>(m, be), m.blockEntityTimes.getLong(be));
                }
            }
            broadcast(server, TOP_10_COUNTS);
            counts.object2LongEntrySet().stream()
                .sorted((a, b) -> Long.compare(b.getLongValue(), a.getLongValue()))
                .limit(10)
                .forEachOrdered(e -> broadcast(server, format(e, (double) e.getLongValue() / duration, 0)));
            broadcast(server, TOP_10_GROSSING);
            times.object2LongEntrySet().stream()
                .sorted((a, b) -> Long.compare(b.getLongValue(), a.getLongValue()))
                .limit(10)
                .forEachOrdered(e -> broadcast(server, c(format(e, e.getLongValue() * divider, msptGoal), s("ms"))));
        }
    }

    public record ReportBoundingBox(RegistryKey<World> dimension, BlockPos from, BlockPos to) {
        public boolean contains(World world, BlockPos pos) {
            if (!dimension.equals(world.getRegistryKey())) return false;
            return pos.getX() >= from.getX() && pos.getX() <= to.getX() &&
                pos.getY() >= from.getY() && pos.getY() <= to.getY() &&
                pos.getZ() >= from.getZ() && pos.getZ() <= to.getZ();
        }
    }

    public static void scheduleEntitiesReport(int ticks, @Nullable ReportBoundingBox bbox) {
        scheduledReport = new EntitiesReport(ticks, bbox);
    }

    public static void scheduleHealthReport(int ticks) {
        scheduledReport = new HealthReport(ticks);
    }

    public static void startSection(World world, SectionType section) {
        if (report instanceof HealthReport r) {
            r.startSection(world, section);
        }
    }

    public static void startEntity(World world, Entity e) {
        if (report instanceof EntitiesReport r && r.contains(world, e.getBlockPos())) {
            r.startEntity(world, e.getType());
        }
    }

    public static void startBlockEntity(World world, BlockEntityTickInvoker ticker) {
        try {
            if (report instanceof EntitiesReport r && r.contains(world, ticker.getPos())) {
                BlockEntityType<?> type = Registry.BLOCK_ENTITY_TYPE.get(new Identifier(ticker.getName()));
                r.startBlockEntity(world, type);
            }
        } catch (RuntimeException e) {
            if (QuickCarpet.isDevelopment()) e.printStackTrace();
        }
    }

    public static void endSection(World world, SectionType expectedType) {
        if (report instanceof HealthReport r) {
            r.endSection(world, expectedType);
        }
    }

    public static void endEntity(World world) {
        if (report instanceof EntitiesReport r) {
            r.endEntity(world);
        }
    }

    public static void endBlockEntity(World world) {
        if (report instanceof EntitiesReport r) {
            r.endBlockEntity(world);
        }
    }

    public static void startTick(MinecraftServer server) {
        currentTickStart = System.nanoTime();
        inTick = true;
        if (scheduledReport != null) {
            report = scheduledReport;
            report.start(server);
            scheduledReport = null;
        }
    }

    public static void endTick(MinecraftServer server) {
        inTick = false;
        if (report != null) {
            report.tick(server);
        }
    }

    private static MutableText format(Object2LongMap.Entry<Pair<Measurement, Object>> entry, double value, double msptGoal) {
        Pair<Measurement, Object> key = entry.getKey();
        Identifier dim = key.getLeft().dimension.getValue();
        Object e = key.getRight();
        Identifier ent = e instanceof EntityType ? EntityType.getId((EntityType<?>) e) : BlockEntityType.getId((BlockEntityType<?>) e);
        return t(Keys.ENTITY_LINE, ent, dim, formats("%.3f", msptGoal == 0 ? Formatting.WHITE : getHeatmapColor(value, msptGoal), value));
    }

    public static void init() {
        try {
            Class.forName("com.sun.management.GcInfo");
            ManagementFactory.getGarbageCollectorMXBeans().forEach(gc -> {
                ((NotificationEmitter) gc).addNotificationListener(CarpetProfiler::handleGCNotification, null, null);
            });
        } catch (ClassNotFoundException e) {
            Loggers.GC.setUnavailable(t(OtherKeys.GC_LOGGER_UNAVAILABLE, System.getProperty("java.vm.name")));
        }
    }

    private static List<LogParameter> getCommandParameters(GarbageCollectionNotificationInfo info) {
        List<LogParameter> params = new ArrayList<>();
        GcInfo gcInfo = info.getGcInfo();
        params.add(new LogParameter("action", info.getGcAction()));
        params.add(new LogParameter("cause", info.getGcCause()));
        params.add(new LogParameter("name", info.getGcName()));
        params.add(new LogParameter("start_time", gcInfo.getStartTime()));
        params.add(new LogParameter("end_time", gcInfo.getEndTime()));
        params.add(new LogParameter("duration", gcInfo.getDuration()));
        for (Map.Entry<String, MemoryUsage> e : gcInfo.getMemoryUsageBeforeGc().entrySet()) {
            MemoryUsage m = e.getValue();
            params.add(new LogParameter("before." + e.getKey() + ".used", m.getUsed()));
            params.add(new LogParameter("before." + e.getKey() + ".committed", m.getCommitted()));
            params.add(new LogParameter("before." + e.getKey() + ".init", m.getInit()));
            params.add(new LogParameter("before." + e.getKey() + ".max", m.getMax()));
        }
        for (Map.Entry<String, MemoryUsage> e : gcInfo.getMemoryUsageAfterGc().entrySet()) {
            MemoryUsage m = e.getValue();
            params.add(new LogParameter("after." + e.getKey() + ".used", m.getUsed()));
            params.add(new LogParameter("after." + e.getKey() + ".committed", m.getCommitted()));
            params.add(new LogParameter("after." + e.getKey() + ".init", m.getInit()));
            params.add(new LogParameter("after." + e.getKey() + ".max", m.getMax()));
        }
        return params;
    }

    private static void handleGCNotification(Notification notification, Object handback) {
        if (!notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) return;
        GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());
        GcInfo gcInfo = info.getGcInfo();
        Loggers.GC.log(() -> {
            long usedBefore = gcInfo.getMemoryUsageBeforeGc().values().stream().mapToLong(MemoryUsage::getUsed).sum();
            long usedAfter = gcInfo.getMemoryUsageAfterGc().values().stream().mapToLong(MemoryUsage::getUsed).sum();
            return c(
                s(info.getGcName(), Formatting.GREEN), s(" "),
                s(info.getGcAction(), Formatting.YELLOW),
                s(" caused by "), s(info.getGcCause(), Formatting.AQUA),
                s(": "), s(info.getGcInfo().getDuration() + "ms", Formatting.AQUA),
                s(", "), s(usedBefore / (1024 * 1024) + "MB", Formatting.AQUA),
                s(" -> "), s(usedAfter / (1024 * 1024) + "MB", Formatting.AQUA)
            );
        }, () -> getCommandParameters(info));
        if (inTick && report != null) {
            for (Measurement m : report.measurements.values()) {
                if (m == null) continue;
                m.gc(gcInfo.getDuration());
            }
        }
    }
}
