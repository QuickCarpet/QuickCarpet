package quickcarpet.module;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

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
}
