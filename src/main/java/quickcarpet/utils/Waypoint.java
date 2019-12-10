package quickcarpet.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import quickcarpet.QuickCarpet;
import quickcarpet.patches.FakeServerPlayerEntity;
import quickcarpet.utils.extensions.WaypointContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

import static quickcarpet.utils.Messenger.*;

public class Waypoint implements Comparable<Waypoint>, Messenger.Formattable {
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(CollectionAdapter.type, new CollectionAdapter()).setPrettyPrinting().create();

    public final @Nonnull
    WaypointContainer world;
    public final @Nonnull String name;
    public final @Nonnull Vec3d position;
    public final @Nonnull Vec2f rotation;
    public final @Nullable String creator;
    public final @Nullable UUID creatorUuid;

    public Waypoint(@Nonnull WaypointContainer world, @Nonnull String name, @Nullable String creator, @Nullable UUID creatorUuid, @Nonnull Vec3d position, @Nonnull Vec2f rotation) {
        this.world = world;
        this.creator = creator;
        this.creatorUuid = creatorUuid;
        this.name = name;
        this.position = position;
        this.rotation = rotation;
    }

    public Waypoint(@Nonnull WaypointContainer world, @Nonnull String name, @Nullable ServerPlayerEntity creator, @Nonnull Vec3d position, @Nonnull Vec2f rotation) {
        this(world, name, creator.getEntityName(), creator.getUuid(), position, rotation);
    }

    public DimensionType getDimension() {
        return world.getDimensionType();
    }

    public String getFullName() {
        return DimensionType.getId(getDimension()) + "/" + name;
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
    public Text format() {
        return s(getDimension().toString()).append(s("/", GRAY)).append(s(name, YELLOW));
    }

    public boolean canManipulate(ServerCommandSource source) {
        if (source.hasPermissionLevel(2)) return true;
        if (creatorUuid == null) return false;
        Entity e = source.getEntity();
        if (!(e instanceof ServerPlayerEntity) || (e instanceof FakeServerPlayerEntity)) return false;
        return creatorUuid.equals(e.getUuid());
    }

    public static Set<Waypoint> getAllWaypoints(Iterable<WaypointContainer> worlds) {
        Set<Waypoint> all = new LinkedHashSet<>();
        for (WaypointContainer world : worlds) all.addAll(world.getWaypoints().values());
        return all;
    }

    @Nullable
    public static Waypoint find(String name, WaypointContainer defaultWorld, Iterable<WaypointContainer> worlds) {
        DimensionType dimension = null;
        int slash = name.indexOf('/');
        if (slash >= 0) {
            dimension = DimensionType.byId(new Identifier(name.substring(0, slash)));
            if (dimension != null) name = name.substring(slash + 1);
        }
        if (dimension == null) {
            Map<String, Waypoint> waypoints = defaultWorld.getWaypoints();
            if (waypoints.containsKey(name)) return waypoints.get(name);
        }
        for (WaypointContainer world : worlds) {
            if (world.getDimensionType() != dimension) continue;
            Map<String, Waypoint> waypoints = world.getWaypoints();
            if (waypoints.containsKey(name)) return waypoints.get(name);
        }
        return null;
    }

    public static Map<String, Waypoint> loadWaypoints(WaypointContainer world) throws IOException {
        File file = getWaypointFile(world);
        if (!file.exists()) return new TreeMap<>();
        try (FileReader reader = new FileReader(file)) {
            Collection<Waypoint> waypoints = GSON.fromJson(reader, CollectionAdapter.type);
            TreeMap<String, Waypoint> map = new TreeMap<>();
            if (waypoints != null) {
                for (Waypoint w : waypoints) map.put(w.name, w);
            }
            return map;
        }
    }

    public static void saveWaypoints(WaypointContainer world) throws IOException {
        File file = getWaypointFile(world);
        Map<String, Waypoint> waypoints = world.getWaypoints();
        if (waypoints.isEmpty()) {
            if (file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
            return;
        }
        try(FileWriter writer = new FileWriter(file)) {
            GSON.toJson(waypoints.values(), CollectionAdapter.type, writer);
        }
    }

    public static File getWaypointFile(WaypointContainer world) {
        return QuickCarpet.getConfigFile("waypoints" + world.getDimensionType().getSuffix() + ".json");
    }

    public static class CollectionAdapter extends TypeAdapter<Collection<Waypoint>> {
        public static final Type type = new TypeToken<Collection<Waypoint>>(){}.getType();

        @Override
        public void write(JsonWriter out, Collection<Waypoint> waypoints) throws IOException {
            out.beginObject();
            for (Waypoint w : waypoints) {
                out.name(w.name);
                out.beginObject();
                out.name("dimension").value(DimensionType.getId(w.getDimension()).toString());
                out.name("x").value(w.position.x);
                out.name("y").value(w.position.y);
                out.name("z").value(w.position.z);
                out.name("yaw").value(w.rotation.y);
                out.name("pitch").value(w.rotation.x);
                out.name("creator").value(w.creator);
                out.name("creatorUuid").value(Objects.toString(w.creatorUuid, null));
                out.endObject();
            }
            out.endObject();
        }

        @Override
        public Collection<Waypoint> read(JsonReader in) throws IOException {
            MinecraftServer server = QuickCarpet.minecraft_server;
            List<Waypoint> waypoints = new ArrayList<>();
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                String creator = null;
                UUID creatorUuid = null;
                WaypointContainer world = (WaypointContainer) server.getWorld(DimensionType.OVERWORLD);
                Double x = null;
                Double y = null;
                Double z = null;
                float yaw = 0;
                float pitch = 0;
                in.beginObject();
                while (in.hasNext()) {
                    switch (in.nextName()) {
                        case "dimension": {
                            world = (WaypointContainer) server.getWorld(DimensionType.byId(new Identifier(in.nextString())));
                            break;
                        }
                        case "x": x = in.nextDouble(); break;
                        case "y": y = in.nextDouble(); break;
                        case "z": z = in.nextDouble(); break;
                        case "yaw": yaw = (float) in.nextDouble(); break;
                        case "pitch": pitch = (float) in.nextDouble(); break;
                        case "creator": creator = in.nextString(); break;
                        case "creatorUuid": creatorUuid = UUID.fromString(in.nextString()); break;
                    }
                }
                in.endObject();
                System.out.printf("%s %s %s,%s,%s %s\n", name, creator, x, y, z, creatorUuid);
                if (x != null && y != null && z != null) {
                    waypoints.add(new Waypoint(world, name, creator, creatorUuid, new Vec3d(x, y, z), new Vec2f(pitch, yaw)));
                }
            }
            in.endObject();
            return waypoints;
        }
    }
}
