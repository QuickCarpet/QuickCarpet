package quickcarpet.utils;

import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import javax.annotation.Nullable;
import java.util.*;

public class CarpetProfiler
{
    private static final Map<DimensionType, Measurement> MEASUREMENTS = new HashMap<>();
    private static int ticksTotal = 0;
    private static int ticksRemaining = 0;
    private static ReportType reportType = null;
    private static long currentTickStart = 0;
    private static long totalTickTime;

    public enum ReportType {
        HEALTH, ENTITIES
    }

    public enum SectionType {
        NETWORK("Network", true),
        AUTOSAVE("Autosave", true),
        SPAWNING("Spawning", false),
        BLOCKS("Blocks", false),
        FLUIDS("Fluids", false),
        ENTITIES("Entities", false),
        BLOCK_ENTITIES("Block Entities", false),
        VILLAGES("Villages", false),
        PORTALS("Portals", false);

        public static final SectionType[] GLOBAL = Arrays.stream(values()).filter(s -> s.global).toArray(SectionType[]::new);
        public static final SectionType[] PER_DIMENSION = Arrays.stream(values()).filter(s -> !s.global).toArray(SectionType[]::new);

        private final String readableName;
        private final boolean global;
        SectionType(String readableName, boolean global) {
            this.readableName = readableName;
            this.global = global;
        }

        @Override
        public String toString() {
            return readableName;
        }
    }

    private static class Measurement {
        final @Nullable DimensionType dimensionType;
        final Object2LongMap<SectionType> sections;
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
            } else {
                this.sections = new Object2LongArrayMap<>(SectionType.PER_DIMENSION, new long[SectionType.PER_DIMENSION.length]);
            }
        }

        void startSection(SectionType type) {
            this.currentSection = type;
            this.currentSectionStart = System.nanoTime();
        }

        void endSection() {
            if (currentSectionStart == 0) throw new IllegalStateException("Section not started");
            sections.put(currentSection, sections.getLong(currentSection) + System.nanoTime() - currentSectionStart);
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
    }

    public static void endTick(MinecraftServer server) {
        if (currentTickStart == 0L || reportType == null) return;
        totalTickTime += System.nanoTime() - currentTickStart;
        if (--ticksRemaining <= 0) {
            finalizeTickReport(server);
        }
    }

    public static boolean isActive() {
        return reportType != null;
    }

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
        Messenger.print_server_message(server, String.format("Average tick time: %.3fms",divider * totalTickTime));
        long accumulated = 0L;

        Measurement global = MEASUREMENTS.get(null);
        for (SectionType section : SectionType.GLOBAL) {
            long nanos = global.sections.getLong(section);
            accumulated += nanos;
            double amount = divider * nanos;
            if (amount > 0.01) {
                Messenger.print_server_message(server, String.format("%s: %.3fms", section, amount));
            }
        }
        for (DimensionType dimensionType : DimensionType.getAll()) {
            Measurement measurement = MEASUREMENTS.get(dimensionType);
            List<String> messages = new ArrayList<>();
            for (SectionType section : SectionType.PER_DIMENSION) {
                long nanos = measurement.sections.getLong(section);
                accumulated += nanos;
                double amount = divider * nanos;
                if (amount > 0.01) {
                    messages.add(String.format(" - %s: %.3fms", section, amount));
                }
            }
            if (!messages.isEmpty()) {
                Messenger.print_server_message(server, String.valueOf(DimensionType.getId(dimensionType)));
                for (String m : messages) Messenger.print_server_message(server, m);
            }
        }

        long rest = totalTickTime - accumulated;

        Messenger.print_server_message(server, String.format("Unknown: %.3fms",divider * rest));
    }

    private static String format(Object2LongMap.Entry<Pair<Measurement, Object>> entry, double value) {
        Pair<Measurement, Object> key = entry.getKey();
        Identifier dim = DimensionType.getId(key.getLeft().dimensionType);
        Object e = key.getRight();
        Identifier ent = e instanceof EntityType ? EntityType.getId((EntityType) e) : BlockEntityType.getId((BlockEntityType) e);
        return String.format(" - %s in %s: %.3f", ent, dim, value);
    }

    private static void finalizeTickEntitiesReport(MinecraftServer server) {
        double divider = 1e-6 / ticksTotal;
        Messenger.print_server_message(server, String.format("Average tick time: %.3fms",divider * totalTickTime));
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
        Messenger.print_server_message(server, "Top 10 counts:");
        counts.object2LongEntrySet().stream()
            .sorted((a, b) -> Long.compare(b.getLongValue(), a.getLongValue()))
            .limit(10)
            .forEachOrdered(e -> Messenger.print_server_message(server, format(e, (double) e.getLongValue() / ticksTotal)));
        Messenger.print_server_message(server, "Top 10 grossing:");
        times.object2LongEntrySet().stream()
            .sorted((a, b) -> Long.compare(b.getLongValue(), a.getLongValue()))
            .limit(10)
            .forEachOrdered(e -> Messenger.print_server_message(server, format(e, e.getLongValue() * divider) + "ms"));
    }
}
