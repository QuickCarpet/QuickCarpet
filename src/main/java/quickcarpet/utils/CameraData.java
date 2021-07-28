package quickcarpet.utils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import quickcarpet.QuickCarpet;
import quickcarpet.QuickCarpetServer;
import quickcarpet.mixin.accessor.TeleportCommandAccessor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record CameraData(RegistryKey<World> dimension, Vec3d position) {
    public static final MapCodec<CameraData> CODEC = RecordCodecBuilder.mapCodec(it -> it.group(
            Identifier.CODEC.fieldOf("dimension").forGetter(d -> d.dimension().getValue()),
            Codec.DOUBLE.fieldOf("x").forGetter(d -> d.position().x),
            Codec.DOUBLE.fieldOf("y").forGetter(d -> d.position().y),
            Codec.DOUBLE.fieldOf("z").forGetter(d -> d.position().z)
    ).apply(it, (dim, x, y, z) -> new CameraData(RegistryKey.of(Registry.WORLD_KEY, dim), new Vec3d(x, y, z))));
    public static final Codec<Map<UUID, CameraData>> MAP_CODEC = Codec.unboundedMap(Codec.STRING.xmap(UUID::fromString, UUID::toString), CODEC.codec());
    private static final Logger LOGGER = LogManager.getLogger("QuickCarpet|CameraData");

    public CameraData(Entity entity) {
        this(entity.world.getRegistryKey(), entity.getPos());
    }

    public boolean restore(Entity entity) {
        MinecraftServer server = entity.world.getServer();
        if (server == null) return false;
        ServerWorld world = server.getWorld(dimension());
        try {
            TeleportCommandAccessor.invokeTeleport(entity.getCommandSource(), entity, world, position().x, position().y, position().z, Collections.emptySet(), entity.getYaw(), entity.getPitch(), null);
        } catch (CommandSyntaxException e) {
            return false;
        }
        return true;
    }

    private static Path getFile() {
        return QuickCarpetServer.getConfigFile(new WorldSavePath("cameraData.json"));
    }

    public static Map<UUID, CameraData> readSaveFile() throws IOException {
        Path file = getFile();
        if (!Files.isRegularFile(file)) return new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            return new HashMap<>(MAP_CODEC.decode(JsonOps.INSTANCE, JsonHelper.deserialize(reader))
                .getOrThrow(false, e -> LOGGER.error("Could not read camera data: {}", e))
                .getFirst());
        }
    }

    public static void writeSaveFile(Map<UUID, CameraData> data) throws IOException {
        Path file = getFile();
        if (data.isEmpty()) {
            Files.deleteIfExists(file);
            return;
        }
        try(BufferedWriter writer = Files.newBufferedWriter(file)) {
            MAP_CODEC.encodeStart(JsonOps.INSTANCE, data)
                .resultOrPartial(e -> LOGGER.error("Could not write camera data: {}", e))
                .ifPresent(obj -> QuickCarpet.GSON.toJson(obj, writer));
        }
    }
}
