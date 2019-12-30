package quickcarpet;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.EnvType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public interface ServerEventListener {
    default Class<?> getSettingsClass() {
        return null;
    }
    default void onServerInit(MinecraftServer server) {}
    default void onServerLoaded(MinecraftServer server) {}
    default void tick(MinecraftServer server) {}
    default void onGameStarted(EnvType type) {
        onGameStarted();
    }
    default void onGameStarted() {}
    default void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {}

    /**
     * Called when a player connects to the server
     * @param player The player connecting
     * @since 2.0.0
     */
    default void onPlayerConnect(ServerPlayerEntity player) {}

    /**
     * Called when a player disconnects to the server
     * @param player The player disconnecting
     * @since 2.0.0
     */
    default void onPlayerDisconnect(ServerPlayerEntity player) {}

    default void onWorldsLoaded(MinecraftServer server) {
        for (ServerWorld world : server.getWorlds()) onWorldLoaded(world);
    }
    default void onWorldLoaded(ServerWorld world) {}

    default void onWorldsSaved(MinecraftServer server) {
        for (ServerWorld world : server.getWorlds()) onWorldSaved(world);
    }
    default void onWorldSaved(ServerWorld world) {}

    default void onWorldsUnloaded(MinecraftServer server) {
        for (ServerWorld world : server.getWorlds()) onWorldUnloaded(world);
    }
    default void onWorldUnloaded(ServerWorld world) {}

    default boolean isIgnoredForRegistrySync(Identifier registry, Identifier entry) {
        return false;
    }
}
