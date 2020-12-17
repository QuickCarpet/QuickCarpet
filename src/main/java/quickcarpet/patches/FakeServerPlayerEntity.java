package quickcarpet.patches;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import quickcarpet.mixin.accessor.ServerNetworkIoAccessor;
import quickcarpet.utils.Messenger;
import quickcarpet.utils.extensions.ActionPackOwner;

public class FakeServerPlayerEntity extends ServerPlayerEntity {
    private boolean hasStartingPos;
    private double startingX, startingY, startingZ;
    private float startingYaw, startingPitch;

    public static FakeServerPlayerEntity createFake(GameProfile profile, MinecraftServer server, double x, double y, double z, double yaw, double pitch, ServerWorld dimension, GameMode gamemode) {
        if (profile.getProperties().containsKey("textures")) {
            profile = SkullBlockEntity.loadProperties(profile);
        }
        FakeServerPlayerEntity instance = new FakeServerPlayerEntity(server, dimension, profile, x, y, z, (float) yaw, (float) pitch);
        ServerPlayerInteractionManager interactionManagerIn = new ServerPlayerInteractionManager(instance);
        FakeClientConnection connection = new FakeClientConnection(NetworkSide.SERVERBOUND);
        ((ServerNetworkIoAccessor) server.getNetworkIo()).getConnections().add(connection);
        server.getPlayerManager().onPlayerConnect(connection, instance);
        if (!instance.world.getRegistryKey().equals(dimension.getRegistryKey())) {
            ServerWorld old_world = (ServerWorld) instance.world;
            old_world.removePlayer(instance, RemovalReason.CHANGED_DIMENSION);
            instance.unsetRemoved();
            dimension.spawnEntity(instance);
            instance.setWorld(dimension);
            server.getPlayerManager().sendWorldInfo(instance, old_world);
            instance.networkHandler.requestTeleport(x, y, z, (float) yaw, (float) pitch);
            instance.interactionManager.setWorld(dimension);
        }
        instance.setHealth(20.0F);
        instance.unsetRemoved();
        instance.networkHandler.requestTeleport(x, y, z, (float) yaw, (float) pitch);
        instance.stepHeight = 0.6F;
        interactionManagerIn.changeGameMode(gamemode);
        server.getPlayerManager().sendToDimension(new EntitySetHeadYawS2CPacket(instance, (byte) (instance.headYaw * 256 / 360)), instance.world.getRegistryKey());
        server.getPlayerManager().sendToDimension(new EntityPositionS2CPacket(instance), instance.world.getRegistryKey());
        instance.getServerWorld().getChunkManager().updateCameraPosition(instance);
        instance.dataTracker.set(PLAYER_MODEL_PARTS, (byte) 0x7f); // show all model layers (incl. capes)
        return instance;
    }

    public static FakeServerPlayerEntity createShadow(MinecraftServer server, ServerPlayerEntity real) {
        server.getPlayerManager().remove(real);
        real.networkHandler.disconnect(new TranslatableText("multiplayer.disconnect.duplicate_login"));
        ServerWorld world = (ServerWorld) real.world;
        GameProfile profile = real.getGameProfile();
        FakeServerPlayerEntity shadow = new FakeServerPlayerEntity(server, world, profile);
        ServerPlayerInteractionManager interactionManager = new ServerPlayerInteractionManager(shadow);
        FakeClientConnection connection = new FakeClientConnection(NetworkSide.SERVERBOUND);
        ((ServerNetworkIoAccessor) server.getNetworkIo()).getConnections().add(connection);
        server.getPlayerManager().onPlayerConnect(connection, shadow);

        shadow.setHealth(real.getHealth());
        shadow.networkHandler.requestTeleport(real.getX(), real.getY(), real.getZ(), real.yaw, real.pitch);
        interactionManager.changeGameMode(real.interactionManager.getGameMode());
        ((ActionPackOwner) shadow).getActionPack().copyFrom(((ActionPackOwner) real).getActionPack());
        shadow.stepHeight = 0.6F;
        shadow.dataTracker.set(PLAYER_MODEL_PARTS, real.getDataTracker().get(PLAYER_MODEL_PARTS));

        server.getPlayerManager().sendToDimension(new EntitySetHeadYawS2CPacket(shadow, (byte) (real.headYaw * 256 / 360)), shadow.world.getRegistryKey());
        server.getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, shadow));
        real.getServerWorld().getChunkManager().updateCameraPosition(shadow);
        return shadow;
    }

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
            ((ActionPackOwner) this).getActionPack().stop();
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
            this.getServerWorld().getChunkManager().updateCameraPosition(this);
        }
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        setHealth(20);
        this.hungerManager = new HungerManager();
        kill();
    }
}
