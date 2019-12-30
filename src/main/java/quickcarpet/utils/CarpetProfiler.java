package quickcarpet.utils;

import com.sun.management.GarbageCollectionNotificationInfo;
import com.sun.management.GcInfo;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import quickcarpet.QuickCarpet;
import quickcarpet.logging.Logger;
import quickcarpet.logging.Loggers;

import javax.annotation.Nullable;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.openmbean.CompositeData;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.*;

import static quickcarpet.utils.Messenger.*;

public class CarpetProfiler
{
    private static final Map<DimensionType, Measurement> MEASUREMENTS = new HashMap<>();
    private static int ticksTotal = 0;
    private static int ticksRemaining = 0;
    private static ReportType reportType = null;
    private static long currentTickStart = 0;
    private static long totalTickTime;
    private static boolean inTick;

    public enum ReportType {
        HEALTH, ENTITIES
    }

    public enum SectionType {
        UNKNOWN(true, false),
        NETWORK(true, false),
        AUTOSAVE(true, true),
        GC(true, true),
        SPAWNING(false, false),
        BLOCKS(false, false),
        FLUIDS(false, false),
        RANDOM_TICKS(false, false),
        BLOCK_EVENTS(false, false),
        ENTITIES(false, false),
        BLOCK_ENTITIES(false, false),
        VILLAGES(false, false),
        PORTALS(false, false);

        public static final SectionType[] GLOBAL = Arrays.stream(values()).filter(s -> s.global).toArray(SectionType[]::new);
        public static final SectionType[] PER_DIMENSION = Arrays.stream(values()).filter(s -> !s.global).toArray(SectionType[]::new);

        private final boolean global;
        private final boolean customFormat;
        private final String translationKey = "carpet.profiler.section." + this.name().toLowerCase(Locale.ROOT);
        SectionType(boolean global, boolean custom) {
            this.global = global;
            this.customFormat = custom;
        }

        public Text getName() {
            return t(translationKey);
        }

        public Text format(double amount, double avgTime) {
            float msptGoal = QuickCarpet.getInstance().tickSpeed.msptGoal;
            if (customFormat) {
                return t(translationKey + ".format", getName(),
                    formats("%.3f", getHeatmapColor(amount, msptGoal), amount),
                    formats("%.3f", getHeatmapColor(avgTime, msptGoal), avgTime)
                );
            }
            return t("carpet.profiler.section.format", getName(),
                formats("%.3f", getHeatmapColor(amount, msptGoal), amount)
            );
        }
    }

    private static class Measurement {
        final @Nullable DimensionType dimensionType;
        final Object2LongMap<SectionType> sections;
        final Object2IntMap<SectionType> sectionCount;
        final Object2LongMap<EntityType> entityTimes = new Object2LongOpenHashMap<>();
        final Object2LongMap<EntityType> entityCount = new Object2LongOpenHashMap<>();
        final Object2LongMap<BlockEntityType> blockEntityTimes = new Object2LongOpenHashMap<>();
        final Object2LongMap<BlockEntityType> blockEntityCount = new Object2LongOpenHashMap<>();

        private SectionType currentSection;
        private long currentSectionStart;

        private EntityType currentEntity;
        private long currentEntityStart;

        private BlockEntityType currentBlockEntity;
        private long currentBlockEntityStart;

        Measurement(@Nullable DimensionType dimensionType) {
            this.dimensionType = dimensionType;
            if (dimensionType == null) {
                this.sections = new Object2LongArrayMap<>(SectionType.GLOBAL, new long[SectionType.GLOBAL.length]);
                this.sectionCount = new Object2IntArrayMap<>(SectionType.GLOBAL, new int[SectionType.GLOBAL.length]);
            } else {
                this.sections = new Object2LongArrayMap<>(SectionType.PER_DIMENSION, new long[SectionType.PER_DIMENSION.length]);
                this.sectionCount = new Object2IntArrayMap<>(SectionType.PER_DIMENSION, new int[SectionType.PER_DIMENSION.length]);
            }
        }

        void startSection(SectionType type) {
            this.currentSection = type;
            this.currentSectionStart = System.nanoTime();
        }

        void endSection() {
            if (currentSectionStart == 0) throw new IllegalStateException("Section not started");
            sections.put(currentSection, sections.getLong(currentSection) + System.nanoTime() - currentSectionStart);
            sectionCount.put(currentSection, sectionCount.getInt(currentSection) + 1);
            currentSectionStart = 0;
        }

        void startEntity(EntityType type) {
            this.currentEntity = type;
            this.currentEntityStart = System.nanoTime();
        }

        void endEntity() {
            long previousTime = entityTimes.getOrDefault(currentEntity, 0);
            entityTimes.put(currentEntity, previousTime + System.nanoTime() - currentEntityStart);
            entityCount.put(currentEntity, entityCount.getOrDefault(currentEntity, 0) + 1);
        }

        void startBlockEntity(BlockEntityType type) {
            this.currentBlockEntity = type;
            this.currentBlockEntityStart = System.nanoTime();
        }

        void endBlockEntity() {
            long previousTime = blockEntityTimes.getOrDefault(currentBlockEntity, 0);
            blockEntityTimes.put(currentBlockEntity, previousTime + System.nanoTime() - currentBlockEntityStart);
            blockEntityCount.put(currentBlockEntity, blockEntityCount.getOrDefault(currentBlockEntity, 0) + 1);
        }

        void gc(long ms) {
            long ns = ms * 1_000_000;
            if (dimensionType == null) {
                sections.put(SectionType.GC, sections.getLong(SectionType.GC) + ns);
                sectionCount.put(SectionType.GC, sectionCount.getInt(SectionType.GC) + 1);
            }
            currentSectionStart += ns;
            currentBlockEntityStart += ns;
            currentEntityStart += ns;
        }
    }

    public static void startTickReport(ReportType type, int ticks) {
        //maybe add so it only spams the sending player, but honestly - all may want to see it
        totalTickTime = 0;
        reportType = type;
        MEASUREMENTS.put(null, new Measurement(null));
        for (DimensionType dimensionType : DimensionType.getAll()) {
            MEASUREMENTS.put(dimensionType, new Measurement(dimensionType));
        }

        ticksRemaining = ticks;
        ticksTotal = ticks;
        currentTickStart = 0L;

    }

    private static Measurement getMeasurement(World world) {
        if (world == null) return MEASUREMENTS.get(null);
        return MEASUREMENTS.get(world.getDimension().getType());
    }

    public static void startSection(World world, SectionType section) {
        if (!isActive(ReportType.HEALTH)) return;
        getMeasurement(world).startSection(section);
    }

    public static void startEntity(World world, Entity e) {
        if (!isActive(ReportType.ENTITIES)) return;
        getMeasurement(world).startEntity(e.getType());
    }

    public static void startBlockEntity(World world, BlockEntity e) {
        if (!isActive(ReportType.ENTITIES)) return;
        getMeasurement(world).startBlockEntity(e.getType());
    }

    public static void endSection(World world) {
        if (!isActive(ReportType.HEALTH)) return;
        getMeasurement(world).endSection();
    }

    public static void endEntity(World world) {
        if (!isActive(ReportType.ENTITIES)) return;
        getMeasurement(world).endEntity();
    }

    public static void endBlockEntity(World world) {
        if (!isActive(ReportType.ENTITIES)) return;
        getMeasurement(world).endBlockEntity();
    }

    public static void startTick() {
        currentTickStart = System.nanoTime();
        inTick = true;
    }

    public static void endTick(MinecraftServer server) {
        inTick = false;
        if (currentTickStart == 0L || reportType == null) return;
        totalTickTime += System.nanoTime() - currentTickStart;
        if (--ticksRemaining <= 0) {
            finalizeTickReport(server);
        }
    }

    public static boolean isActive() {
        return reportType != null;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isActive(ReportType type) {
        return reportType == type;
    }

    private static void finalizeTickReport(MinecraftServer server) {
        if (reportType == ReportType.HEALTH) finalizeTickHealthReport(server);
        else if (reportType == ReportType.ENTITIES) finalizeTickEntitiesReport(server);
        reportType = null;
    }

    private static void finalizeTickHealthReport(MinecraftServer server) {
        double divider = 1e-6 / ticksTotal;
        double avgTickTime = divider * totalTickTime;
        broadcast(server, t("carpet.profiler.title", formats("%.3f", getHeatmapColor(avgTickTime, 50), avgTickTime)));
        long accumulated = 0L;

        Measurement global = MEASUREMENTS.get(null);
        for (SectionType section : SectionType.GLOBAL) {
            long nanos = global.sections.getLong(section);
            accumulated += nanos;
            double amount = divider * nanos;
            double avgTime = nanos / (1e6 * global.sectionCount.getInt(section));
            if (amount > 0.01 || avgTime > 0.1) {
                broadcast(server, section.format(amount, avgTime));
            }
        }
        for (DimensionType dimensionType : DimensionType.getAll()) {
            Measurement measurement = MEASUREMENTS.get(dimensionType);
            List<Text> messages = new ArrayList<>();
            for (SectionType section : SectionType.PER_DIMENSION) {
                long nanos = measurement.sections.getLong(section);
                accumulated += nanos;
                double amount = divider * nanos;
                double avgTime = nanos / (1e6 * measurement.sectionCount.getInt(section));
                if (amount > 0.01 || avgTime > 0.1) {
                    messages.add(c(s(" - "), section.format(amount, avgTime)));
                }
            }
            if (!messages.isEmpty()) {
                broadcast(server, String.valueOf(DimensionType.getId(dimensionType)));
                for (Text m : messages) broadcast(server, m);
            }
        }

        long rest = totalTickTime - accumulated;

        broadcast(server, SectionType.UNKNOWN.format(divider * rest, 0));
    }

    private static Text format(Object2LongMap.Entry<Pair<Measurement, Object>> entry, double value, double msptGoal) {
        Pair<Measurement, Object> key = entry.getKey();
        Identifier dim = DimensionType.getId(key.getLeft().dimensionType);
        Object e = key.getRight();
        Identifier ent = e instanceof EntityType ? EntityType.getId((EntityType) e) : BlockEntityType.getId((BlockEntityType) e);
        return t("carpet.profiler.entity.line", ent, dim, formats("%.3f", msptGoal == 0 ? WHITE : getHeatmapColor(value, msptGoal), value));
    }

    private static void finalizeTickEntitiesReport(MinecraftServer server) {
        double divider = 1e-6 / ticksTotal;
        double avgTickTime = divider * totalTickTime;
        float msptGoal = QuickCarpet.getInstance().tickSpeed.msptGoal;
        broadcast(server, t("carpet.profiler.title", formats("%.3f", getHeatmapColor(avgTickTime, msptGoal), avgTickTime)));
        Object2LongMap<Pair<Measurement, Object>> counts = new Object2LongOpenHashMap<>();
        Object2LongMap<Pair<Measurement, Object>> times = new Object2LongOpenHashMap<>();
        for (Measurement m : MEASUREMENTS.values()) {
            for (EntityType e : m.entityCount.keySet()) {
                counts.put(new Pair<>(m, e), m.entityCount.getLong(e));
                times.put(new Pair<>(m, e), m.entityTimes.getLong(e));
            }
            for (BlockEntityType be : m.blockEntityCount.keySet()) {
                counts.put(new Pair<>(m, be), m.blockEntityCount.getLong(be));
                times.put(new Pair<>(m, be), m.blockEntityTimes.getLong(be));
            }
        }
        broadcast(server, t("carpet.profiler.top_10_counts"));
        counts.object2LongEntrySet().stream()
            .sorted((a, b) -> Long.compare(b.getLongValue(), a.getLongValue()))
            .limit(10)
            .forEachOrdered(e -> broadcast(server, format(e, (double) e.getLongValue() / ticksTotal, 0)));
        broadcast(server, t("carpet.profiler.top_10_grossing"));
        times.object2LongEntrySet().stream()
            .sorted((a, b) -> Long.compare(b.getLongValue(), a.getLongValue()))
            .limit(10)
            .forEachOrdered(e -> broadcast(server, c(format(e, e.getLongValue() * divider, msptGoal), s("ms"))));
    }

    public static void init() {
        try {
            Class.forName("com.sun.management.GcInfo");
            ManagementFactory.getGarbageCollectorMXBeans().forEach(gc -> {
                ((NotificationEmitter) gc).addNotificationListener(CarpetProfiler::handleGCNotification, null, null);
            });
        } catch (ClassNotFoundException e) {
            Loggers.GC.setUnavailable(t("logger.gc.unavailable", System.getProperty("java.vm.name")));
        }
    }

    public static class GCCommandParameters extends LinkedHashMap<String, Object> implements Logger.CommandParameters<Object> {
        public final GarbageCollectionNotificationInfo info;

        private GCCommandParameters(GarbageCollectionNotificationInfo info) {
            this.info = info;
            this.put("action", info.getGcAction());
            this.put("cause", info.getGcCause());
            this.put("name", info.getGcName());
            GcInfo gcInfo = info.getGcInfo();
            this.put("start_time", gcInfo.getStartTime());
            this.put("end_time", gcInfo.getEndTime());
            this.put("duration", gcInfo.getDuration());
            for (Map.Entry<String, MemoryUsage> e : gcInfo.getMemoryUsageBeforeGc().entrySet()) {
                MemoryUsage m = e.getValue();
                this.put("before." + e.getKey() + ".used", m.getUsed());
                this.put("before." + e.getKey() + ".committed", m.getCommitted());
                this.put("before." + e.getKey() + ".init", m.getInit());
                this.put("before." + e.getKey() + ".max", m.getMax());
            }
            for (Map.Entry<String, MemoryUsage> e : gcInfo.getMemoryUsageAfterGc().entrySet()) {
                MemoryUsage m = e.getValue();
                this.put("after." + e.getKey() + ".used", m.getUsed());
                this.put("after." + e.getKey() + ".committed", m.getCommitted());
                this.put("after." + e.getKey() + ".init", m.getInit());
                this.put("after." + e.getKey() + ".max", m.getMax());
            }
        }
    }

    private static void handleGCNotification(Notification notification, Object handback) {
        if (!notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) return;
        GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());
        GcInfo gcInfo = info.getGcInfo();
        Loggers.GC.log(() -> {
            long usedBefore = gcInfo.getMemoryUsageBeforeGc().values().stream().mapToLong(MemoryUsage::getUsed).sum();
            long usedAfter = gcInfo.getMemoryUsageAfterGc().values().stream().mapToLong(MemoryUsage::getUsed).sum();
            return new Text[]{Messenger.c(
                    "l " + info.getGcName(),
                    "y  " + info.getGcAction(),
                    "w  caused by ", "c " + info.getGcCause(),
                    "w : ", "c " + info.getGcInfo().getDuration() + "ms",
                    "w , ", "c " + usedBefore / (1024 * 1024) + "MB",
                    "w  -> ", "c " + usedAfter / (1024 * 1024) + "MB")
            };
        }, () -> new GCCommandParameters(info));
        if (inTick) {
            if (!isActive(ReportType.HEALTH)) return;
            for (Measurement m : MEASUREMENTS.values()) {
                if (m == null) continue;
                m.gc(gcInfo.getDuration());
            }
        }
    }
}
