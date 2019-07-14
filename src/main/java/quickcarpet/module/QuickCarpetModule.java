package quickcarpet.module;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.Nonnull;

public interface QuickCarpetModule extends Comparable<QuickCarpetModule> {
    String getName();
    String getVersion();
    String getId();

    @Override
    default int compareTo(@Nonnull QuickCarpetModule o) {
        return getId().compareTo(o.getId());
    }

    default Class<?> getSettingsClass() {
        return null;
    }
    default void onServerInit(MinecraftServer server) {}
    default void onServerLoaded(MinecraftServer server) {}
    default void tick(MinecraftServer server) {}
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
}
