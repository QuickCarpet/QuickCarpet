package quickcarpet.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import quickcarpet.QuickCarpetServer;
import quickcarpet.patches.FakeServerPlayerEntity;
import quickcarpet.utils.extensions.WaypointContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class UnnamedWaypoint {
    public final @Nonnull WaypointContainer world;
    public final @Nonnull Vec3d position;
    public final @Nonnull Vec2f rotation;
    public final @Nullable String creator;
    public final @Nullable UUID creatorUuid;

    public UnnamedWaypoint(@Nonnull WaypointContainer world, @Nullable String creator, @Nullable UUID creatorUuid, @Nonnull Vec3d position, @Nonnull Vec2f rotation) {
        this.world = world;
        this.creator = creator;
        this.creatorUuid = creatorUuid;
        this.position = position;
        this.rotation = rotation;
    }

    public RegistryKey<World> getDimension() {
        return world.getWaypointWorldKey();
    }

    public boolean canManipulate(ServerCommandSource source) {
        if (source.hasPermissionLevel(2)) return true;
        if (creatorUuid == null) return false;
        Entity e = source.getEntity();
        if (!(e instanceof ServerPlayerEntity) || (e instanceof FakeServerPlayerEntity)) return false;
        return creatorUuid.equals(e.getUuid());
    }

    public Waypoint named(String name) {
        return new Waypoint(world, name, creator, creatorUuid, position, rotation);
    }

    public static final MapCodec<UnnamedWaypoint> CODEC = RecordCodecBuilder.mapCodec(it -> it.group(
            Identifier.CODEC.fieldOf("dimension").forGetter(w -> w.getDimension().getValue()),
            Codec.DOUBLE.fieldOf("x").forGetter(w -> w.position.x),
            Codec.DOUBLE.fieldOf("y").forGetter(w -> w.position.y),
            Codec.DOUBLE.fieldOf("z").forGetter(w -> w.position.z),
            Codec.FLOAT.optionalFieldOf("yaw", 0f).forGetter(w -> w.rotation.y),
            Codec.FLOAT.optionalFieldOf("pitch", 0f).forGetter(w -> w.rotation.x),
            Codec.STRING.fieldOf("creator").orElse(null).forGetter(w -> w.creator),
            Codec.STRING.optionalFieldOf("creatorUuid").xmap(s -> s.map(UUID::fromString).orElse(null), u -> Optional.ofNullable(u).map(UUID::toString)).forGetter(w -> w.creatorUuid)
    ).apply(it, (dim, x, y, z, yaw, pitch, creator, uuid) -> {
        MinecraftServer server = QuickCarpetServer.getMinecraftServer();
        WaypointContainer world = (WaypointContainer) server.getWorld(RegistryKey.of(Registry.DIMENSION, dim));
        return new UnnamedWaypoint(world, creator, uuid, new Vec3d(x, y, z), new Vec2f(pitch, yaw));
    }));

    public static Codec<Map<String, UnnamedWaypoint>> MAP_CODEC = Codec.unboundedMap(Codec.STRING, UnnamedWaypoint.CODEC.codec());
}
