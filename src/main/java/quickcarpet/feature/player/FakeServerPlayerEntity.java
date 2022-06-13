package quickcarpet.feature.player;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import quickcarpet.QuickCarpet;
import quickcarpet.QuickCarpetServer;
import quickcarpet.mixin.accessor.ServerNetworkIoAccessor;
import quickcarpet.settings.Settings;
import quickcarpet.utils.Messenger;
import quickcarpet.utils.mixin.extensions.ActionPackOwner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FakeServerPlayerEntity extends ServerPlayerEntity {
    private static final Logger LOGGER = LogManager.getLogger("QuickCarpet|Bots");
    private boolean hasStartingPos;
    private double startingX, startingY, startingZ;
    private float startingYaw, startingPitch;

    private FakeServerPlayerEntity(MinecraftServer server, ServerWorld world, GameProfile profile) {
        super(server, world, profile);
        this.hasStartingPos = false;
    }

    private FakeServerPlayerEntity(MinecraftServer server, ServerWorld world, GameProfile profile, double x, double y, double z, float yaw, float pitch) {
        super(server, world, profile);
        this.hasStartingPos = true;
        this.startingX = x;
        this.startingY = y;
        this.startingZ = z;
        this.startingYaw = yaw;
        this.startingPitch = pitch;
    }

    public void applyStartingPosition() {
        if (hasStartingPos) {
            this.refreshPositionAndAngles(startingX, startingY, startingZ, startingYaw, startingPitch);
            this.setVelocity(Vec3d.ZERO);
        }
    }

    @Override
    public void kill() {
        this.server.send(new ServerTask(this.server.getTicks(), () -> {
            ((ActionPackOwner) this).quickcarpet$getActionPack().stop();
            this.networkHandler.onDisconnected(Messenger.s("Killed"));
        }));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isInTeleportationState()) {
            this.onTeleportationDone();
        }
        this.tickMovement();
        if (this.getServer().getTicks() % 10 == 0) {
            this.networkHandler.syncWithPlayerPosition();
            this.getWorld().getChunkManager().updatePosition(this);
        }
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        setHealth(20);
        this.hungerManager = new HungerManager();
        kill();
    }

    public static void createFake(GameProfile profile, MinecraftServer server, double x, double y, double z, double yaw, double pitch, ServerWorld dimension, GameMode gamemode, boolean flying) {
        if (profile.getProperties().containsKey("textures")) {
            profile = server.getSessionService().fillProfileProperties(profile, false);
            server.getUserCache().add(profile);
        }
        FakeServerPlayerEntity player = new FakeServerPlayerEntity(server, dimension, profile, x, y, z, (float) yaw, (float) pitch);
        FakeClientConnection connection = new FakeClientConnection(NetworkSide.SERVERBOUND);
        ((ServerNetworkIoAccessor) server.getNetworkIo()).getConnections().add(connection);
        server.getPlayerManager().onPlayerConnect(connection, player);
        if (!player.world.getRegistryKey().equals(dimension.getRegistryKey())) {
            ServerWorld old_world = (ServerWorld) player.world;
            old_world.removePlayer(player, RemovalReason.CHANGED_DIMENSION);
            player.unsetRemoved();
            dimension.spawnEntity(player);
            player.setWorld(dimension);
            server.getPlayerManager().sendWorldInfo(player, old_world);
            player.networkHandler.requestTeleport(x, y, z, (float) yaw, (float) pitch);
            player.interactionManager.setWorld(dimension);
        }
        player.setHealth(20.0F);
        player.unsetRemoved();
        PlayerAbilities abilities = player.getAbilities();
        abilities.flying = abilities.allowFlying && flying;
        player.networkHandler.requestTeleport(x, y, z, (float) yaw, (float) pitch);
        player.interactionManager.changeGameMode(gamemode);
        postCreate(server, player);
    }

    public static FakeServerPlayerEntity createFake(GameProfile profile, MinecraftServer server) {
        FakeServerPlayerEntity player = new FakeServerPlayerEntity(server, server.getOverworld(), profile);
        FakeClientConnection connection = new FakeClientConnection(NetworkSide.SERVERBOUND);
        ((ServerNetworkIoAccessor) server.getNetworkIo()).getConnections().add(connection);
        server.getPlayerManager().onPlayerConnect(connection, player);
        player.setHealth(20.0F);
        player.unsetRemoved();
        postCreate(server, player);
        return player;
    }

    private static void postCreate(MinecraftServer server, FakeServerPlayerEntity player) {
        player.stepHeight = 0.6F;
        server.getPlayerManager().sendToDimension(new EntitySetHeadYawS2CPacket(player, (byte) (player.headYaw * 256 / 360)), player.world.getRegistryKey());
        server.getPlayerManager().sendToDimension(new EntityPositionS2CPacket(player), player.world.getRegistryKey());
        player.getWorld().getChunkManager().updatePosition(player);
        player.dataTracker.set(PLAYER_MODEL_PARTS, (byte) 0x7f); // show all model layers (incl. capes)
    }

    public static void createShadow(MinecraftServer server, ServerPlayerEntity real) {
        Text reason = new TranslatableText("multiplayer.disconnect.duplicate_login");
        real.networkHandler.onDisconnected(reason);
        real.networkHandler.disconnect(reason);
        ServerWorld world = (ServerWorld) real.world;
        GameProfile profile = real.getGameProfile();
        FakeServerPlayerEntity shadow = new FakeServerPlayerEntity(server, world, profile);
        FakeClientConnection connection = new FakeClientConnection(NetworkSide.SERVERBOUND);
        ((ServerNetworkIoAccessor) server.getNetworkIo()).getConnections().add(connection);
        server.getPlayerManager().onPlayerConnect(connection, shadow);

        shadow.setHealth(real.getHealth());
        shadow.networkHandler.requestTeleport(real.getX(), real.getY(), real.getZ(), real.getYaw(), real.getPitch());
        shadow.stepHeight = 0.6F;
        shadow.interactionManager.changeGameMode(real.interactionManager.getGameMode());
        server.getPlayerManager().sendToDimension(new EntitySetHeadYawS2CPacket(shadow, (byte) (real.headYaw * 256 / 360)), shadow.world.getRegistryKey());
        server.getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, shadow));
        real.getWorld().getChunkManager().updatePosition(shadow);
        shadow.dataTracker.set(PLAYER_MODEL_PARTS, real.getDataTracker().get(PLAYER_MODEL_PARTS));
        ((ActionPackOwner) shadow).quickcarpet$getActionPack().copyFrom(((ActionPackOwner) real).quickcarpet$getActionPack());
    }

    private static final Codec<Map<UUID, PlayerActionPack.State>> BOTS_CODEC = Codec.unboundedMap(Codec.STRING.xmap(UUID::fromString, UUID::toString), PlayerActionPack.State.CODEC.codec());

    private static Path getFile() {
        return QuickCarpetServer.getConfigFile(new WorldSavePath("bots.json"));
    }

    public static void loadPersistent(MinecraftServer server) throws IOException {
        if (!Settings.persistentPlayers) return;
        Path file = getFile();
        if (!Files.isRegularFile(file)) return;
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            Map<UUID, PlayerActionPack.State> bots =  new LinkedHashMap<>(BOTS_CODEC.decode(JsonOps.INSTANCE, JsonHelper.deserialize(reader))
                .getOrThrow(false, e -> LOGGER.error("Could not read persistent players: {}", e))
                .getFirst());
            for (var e : bots.entrySet()) {
                loginBot(server, e.getKey(), e.getValue());
            }
        }
    }

    private static void loginBot(MinecraftServer server, UUID uuid, PlayerActionPack.State state) {
        GameProfile profile = server.getUserCache().getByUuid(uuid).orElse(null);
        if (profile == null) {
            profile = new GameProfile(uuid, null);
        }
        if (!profile.isComplete()) {
            profile = server.getSessionService().fillProfileProperties(profile, true);
        }
        GameProfile filledProfile = server.getSessionService().fillProfileProperties(profile, true);
        server.send(new ServerTask(server.getTicks(), () -> {
            FakeServerPlayerEntity player = createFake(filledProfile, server);
            ((ActionPackOwner) player).quickcarpet$setActionPack(new PlayerActionPack(player, state));
        }));
    }

    public static void savePersistent(MinecraftServer server) throws IOException {
        if (!Settings.persistentPlayers) return;
        Map<UUID, PlayerActionPack.State> bots = new LinkedHashMap<>();
        List<ServerPlayerEntity> allPlayers = server.getPlayerManager().getPlayerList();
        if (allPlayers.isEmpty()) return;
        for (var player : allPlayers) {
            if (!(player instanceof FakeServerPlayerEntity)) continue;
            var state = ((ActionPackOwner) player).quickcarpet$getActionPack().getState();
            bots.put(player.getUuid(), state);
        }
        Path file = getFile();
        if (bots.isEmpty()) {
            Files.deleteIfExists(file);
            return;
        }

        try(BufferedWriter writer = Files.newBufferedWriter(file)) {
            BOTS_CODEC.encodeStart(JsonOps.INSTANCE, bots)
                .resultOrPartial(e -> LOGGER.error("Could not write persistent players: {}", e))
                .ifPresent(obj -> QuickCarpet.GSON.toJson(obj, writer));
        }
    }
}
