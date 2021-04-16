package quickcarpet.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import quickcarpet.QuickCarpetServer;
import quickcarpet.utils.extensions.WaypointContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static quickcarpet.utils.Messenger.c;
import static quickcarpet.utils.Messenger.s;

public class Waypoint extends UnnamedWaypoint implements Comparable<Waypoint>, Messenger.Formattable {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Codec<Map<String, Waypoint>> MAP_CODEC = UnnamedWaypoint.MAP_CODEC.xmap(map -> {
        Map<String, Waypoint> map2 = new LinkedHashMap<>();
        for (Map.Entry<String, UnnamedWaypoint> e : map.entrySet()) map2.put(e.getKey(), e.getValue().named(e.getKey()));
        return map2;
    }, map -> (Map<String, UnnamedWaypoint>) (Map) map);

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogManager.getLogger();

    public final @Nonnull String name;

    public Waypoint(@Nonnull WaypointContainer world, @Nonnull String name, @Nullable String creator, @Nullable UUID creatorUuid, @Nonnull Vec3d position, @Nonnull Vec2f rotation) {
        super(world, creator, creatorUuid, position, rotation);
        this.name = name;
    }

    public Waypoint(@Nonnull WaypointContainer world, @Nonnull String name, @Nullable ServerPlayerEntity creator, @Nonnull Vec3d position, @Nonnull Vec2f rotation) {
        this(world, name, creator == null ? null : creator.getEntityName(), creator == null ? null : creator.getUuid(), position, rotation);
    }

    public String getFullName() {
        return getDimension().getValue() + "/" + name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this.getClass() != obj.getClass()) return false;
        Waypoint w = (Waypoint) obj;
        return world.equals(w.world) && name.equals(w.name);
    }

    @Override
    public int hashCode() {
        return world.hashCode() ^ name.hashCode();
    }

    @Override
    public int compareTo(@Nonnull Waypoint o) {
        if (this.equals(o)) return 0;
        if (this.world == o.world) return name.compareTo(o.name);
        return getFullName().compareTo(o.getFullName());
    }

    @Override
    public String toString() {
        return "Waypoint[" + getFullName() + "," + position + "," + rotation + "]";
    }

    @Override
    public MutableText format() {
        return c(s(getDimension().getValue().toString()), s("/", Formatting. GRAY), s(name, Formatting.YELLOW));
    }

    public static Set<Waypoint> getAllWaypoints(Iterable<WaypointContainer> worlds) {
        Set<Waypoint> all = new LinkedHashSet<>();
        for (WaypointContainer world : worlds) all.addAll(world.getWaypoints().values());
        return all;
    }

    @Nullable
    public static Waypoint find(String name, WaypointContainer defaultWorld, Iterable<WaypointContainer> worlds) {
        RegistryKey<World> dimension = null;
        int slash = name.indexOf('/');
        if (slash >= 0) {
            dimension = RegistryKey.of(Registry.WORLD_KEY, new Identifier(name.substring(0, slash)));
            if (dimension != null) name = name.substring(slash + 1);
        }
        if (dimension == null) {
            Map<String, Waypoint> waypoints = defaultWorld.getWaypoints();
            if (waypoints.containsKey(name)) return waypoints.get(name);
        }
        for (WaypointContainer world : worlds) {
            if (world.getWaypointWorldKey() != dimension) continue;
            Map<String, Waypoint> waypoints = world.getWaypoints();
            if (waypoints.containsKey(name)) return waypoints.get(name);
        }
        return null;
    }

    public static Map<String, Waypoint> loadWaypoints(WaypointContainer world) throws IOException {
        Path file = getWaypointFile(world);
        if (!Files.exists(file)) return new TreeMap<>();
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            return MAP_CODEC.decode(JsonOps.INSTANCE, JsonHelper.deserialize(reader))
                .getOrThrow(false, e -> LOGGER.error("Could not read waypoints: {}", e))
                .getFirst();
        }
    }

    public static void saveWaypoints(WaypointContainer world) throws IOException {
        Path file = getWaypointFile(world);
        Map<String, Waypoint> waypoints = world.getWaypoints();
        if (waypoints.isEmpty()) {
            Files.deleteIfExists(file);
            return;
        }
        try(BufferedWriter writer = Files.newBufferedWriter(file)) {
            MAP_CODEC.encodeStart(JsonOps.INSTANCE, waypoints)
                .resultOrPartial(e -> LOGGER.error("Could not write waypoints: {}", e))
                .ifPresent(obj -> GSON.toJson(obj, writer));
        }
    }

    public static Path getWaypointFile(WaypointContainer world) {
        File rootFile = new File(".");
        File saveDirFile = DimensionType.getSaveDirectory(world.getWaypointWorldKey(), rootFile);
        Path relPath = rootFile.toPath().relativize(saveDirFile.toPath()).resolve("data/waypoints.json");
        return QuickCarpetServer.getConfigFile(WorldSavePath.ROOT).resolve(relPath);
    }
}
