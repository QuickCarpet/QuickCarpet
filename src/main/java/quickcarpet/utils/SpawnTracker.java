package quickcarpet.utils;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.stream.Stream;

import static quickcarpet.utils.Messenger.*;

public class SpawnTracker {
    private static final Map<ServerCommandSource, SpawnTracker> TRACKERS = new WeakHashMap<>();
    private final ServerCommandSource source;
    private final Box bbox;
    private final DimensionType dimension;
    private boolean active;
    private int tickStarted = -1;
    private int tickStopped = -1;

    private final Object2IntMap<EntityType<?>> successfulSpawns = new Object2IntOpenHashMap<>();
    private final Object2IntMap<SpawnGroup> mobcapFull = new Object2IntOpenHashMap<>();
    private final Object2IntMap<SpawnGroup> mobcapNotFull = new Object2IntOpenHashMap<>();
    private final Object2IntMap<EntityType<?>> attempts = new Object2IntOpenHashMap<>();

    private SpawnTracker(ServerCommandSource source, BlockPos min, BlockPos max) {
        this.source = source;
        if (min != null && max != null) {
            this.bbox = new Box(min, max).stretch(1, 1, 1);
        } else {
            this.bbox = null;
        }
        this.dimension = source.getWorld().getDimension();
    }

    public void start() {
        this.active = true;
        this.tickStarted = source.getServer().getTicks();
    }

    public void stop() {
        this.active = false;
        this.tickStopped = source.getServer().getTicks();
        TRACKERS.remove(source);
    }

    public void sendReport() {
        try {
            if (this.tickStarted < 0) return;
            int tickTo = this.tickStopped >= 0 ? this.tickStopped : this.source.getServer().getTicks();
            int ticksActive = tickTo - this.tickStarted;
            double seconds = ticksActive / 20.;
            int minutes = (int) seconds / 60;
            int hours = minutes / 60;
            m(source, ts("command.spawn.tracking.title", Formatting.DARK_GREEN, formats("%d:%02d:%05.2f", Formatting.LIGHT_PURPLE, hours, minutes % 60, seconds % 60)));
            double perHour = 72000. / ticksActive;
            Set<SpawnGroup> seenEntityCategories = new TreeSet<>();
            seenEntityCategories.addAll(mobcapFull.keySet());
            seenEntityCategories.addAll(mobcapNotFull.keySet());
            for (SpawnGroup category : seenEntityCategories) {
                int mobcapFullCount = mobcapFull.getOrDefault(category, 0);
                int mobcapNotFullCount = mobcapNotFull.getOrDefault(category, 0);
                int totalGlobalAttempts = mobcapFullCount + mobcapNotFullCount;
                double mobcapFullPortion = (double) mobcapFullCount / totalGlobalAttempts;
                m(source, ts("command.spawn.tracking.category", Formatting.GRAY,
                    s(category.getName(), Formatting.WHITE),
                    formats("%.2f", getHeatmapColor(mobcapFullPortion, 1), 100 * mobcapFullPortion)
                ));
                attempts.keySet().stream().filter(t -> t.getSpawnGroup() == category).sorted(SpawnTracker::sortEntityType).forEach(type -> {
                    int successful = successfulSpawns.getOrDefault(type, 0);
                    int numAttempts = attempts.getOrDefault(type, 0);
                    double successfulPerHour = successful * perHour;
                    double successfulPortion = (double) successful / numAttempts;
                    m(source, ts("command.spawn.tracking.mob", Formatting.GRAY,
                        s(EntityType.getId(type).toString(), Formatting.WHITE),
                        s(Integer.toString(successful), Formatting.DARK_GREEN),
                        c(
                            s(Integer.toString((int) successfulPerHour), Formatting.DARK_GREEN),
                            s(String.format("%.2f", successfulPerHour % 1).replace("0.", "."), Formatting.DARK_AQUA)
                        ),
                        formats("%.2f", getHeatmapColor(1 - successfulPortion, 1), 100 * successfulPortion)
                    ));
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public static SpawnTracker getTracker(ServerCommandSource source) {
        return TRACKERS.get(source);
    }

    public static SpawnTracker getOrCreateTracker(ServerCommandSource source, BlockPos min, BlockPos max) {
        return TRACKERS.computeIfAbsent(source, s -> new SpawnTracker(source, min, max));
    }

    public static boolean isAnyActive() {
        if (TRACKERS.isEmpty()) return false;
        for (SpawnTracker tracker : TRACKERS.values()) {
            if (tracker.active) return true;
        }
        return false;
    }

    public boolean isActive() {
        return active;
    }

    private static Stream<SpawnTracker> getTrackersAt(Entity entity) {
        return getTrackersAt(entity.getEntityWorld().getDimension(), entity.getPos());
    }

    private static Stream<SpawnTracker> getTrackersAt(DimensionType d, Vec3d pos) {
        return TRACKERS.values().stream().filter(t -> {
            if (!t.active) return false;
            return t.contains(d, pos);
        });
    }

    private static Stream<SpawnTracker> getTrackersAt(DimensionType d) {
        return TRACKERS.values().stream().filter(t -> {
            if (!t.active) return false;
            return t.dimension == d;
        });
    }

    public boolean contains(DimensionType d, Vec3d pos) {
        if (this.dimension != d) return false;
        if (bbox == null) return true;
        return bbox.contains(pos);
    }

    private <T> void increment(Object2IntMap<T> map, T key, int amount) {
        map.put(key, map.getOrDefault(key, 0) + amount);
    }


    public static void registerSpawn(Entity entity) {
        if (!isAnyActive()) return;
        getTrackersAt(entity).forEach(t -> t.registerSpawn0(entity));
    }

    private void registerSpawn0(Entity entity) {
        increment(successfulSpawns, entity.getType(), 1);
    }

    public static void registerMobcapStatus(DimensionType dim, SpawnGroup category, boolean full) {
        if (!isAnyActive()) return;
        getTrackersAt(dim).forEach(t -> t.registerMobcapStatus(category, full));
    }

    private void registerMobcapStatus(SpawnGroup category, boolean full) {
        if (full) increment(mobcapFull, category, 1);
        else increment(mobcapNotFull, category, 1);
    }

    public static void registerAttempt(DimensionType dim, Vec3d pos, EntityType<?> type) {
        if (!isAnyActive()) return;
        getTrackersAt(dim, pos).forEach(t -> t.registerAttempt(pos, type));
    }

    private void registerAttempt(Vec3d pos, EntityType<?> type) {
        increment(attempts, type, 1);
    }

    private static int sortEntityType(EntityType<?> a, EntityType<?> b) {
        if (a == b) return 0;
        SpawnGroup categoryA = a.getSpawnGroup();
        SpawnGroup categoryB = b.getSpawnGroup();
        if (categoryA == categoryB) {
            String idA = EntityType.getId(a).toString();
            String idB = EntityType.getId(b).toString();
            return idA.compareTo(idB);
        }
        return categoryA.compareTo(categoryB);
    }
}
