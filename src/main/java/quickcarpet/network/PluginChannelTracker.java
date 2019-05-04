package quickcarpet.network;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Set;
import java.util.stream.Collectors;

public class PluginChannelTracker {
    private final MinecraftServer server;

    // A multimap from player names to the channels they are registered on
    private SetMultimap<String, Identifier> name2channels = MultimapBuilder.hashKeys().hashSetValues().build();

    // A multimap from channel names to the names of players registered to that channel
    private SetMultimap<Identifier, String> channel2names = MultimapBuilder.hashKeys().hashSetValues().build();

    public PluginChannelTracker(MinecraftServer server) {
        this.server = server;
    }

    /**
     * Returns the collection of channels {@code player} is registered to.
     */
    public Set<Identifier> getChannels(ServerPlayerEntity player) {
        return name2channels.get(player.getEntityName());
    }

    /**
     * Returns whether or not {@code player} is reigstered to {@code channel}.
     */
    public boolean isRegistered(ServerPlayerEntity player, Identifier channel) {
        return name2channels.containsEntry(player.getEntityName(), channel);
    }

    /**
     * Returns the collection of names of players registered to {@code channel}.
     */
    public Set<String> getPlayerNames(Identifier channel) {
        return channel2names.get(channel);
    }

    /**
     * Returns the collection of players registered to {@code channel}. The {@code server} is used to look players up
     * by their name.
     */
    public Set<ServerPlayerEntity> getPlayers(Identifier channel) {
        PlayerManager pl = server.getPlayerManager();
        return channel2names.get(channel).stream()
                .map(pl::getPlayer)
                .collect(Collectors.toSet());
    }

    /**
     * Registers {@code player} on {@code channel}.
     */
    public void register(ServerPlayerEntity player, Identifier channel) {
        name2channels.put(player.getEntityName(), channel);
        channel2names.put(channel, player.getEntityName());
    }

    /**
     * Unregisters {@code player} from {@code channel}.
     */
    public void unregister(ServerPlayerEntity player, Identifier channel) {
        name2channels.remove(player.getName(), channel);
        channel2names.remove(channel, player.getName());
    }

    /**
     * Unregisters {@code player} from all channels.
     */
    public void unregisterAll(ServerPlayerEntity player) {
        for (Identifier channel : getChannels(player)) {
            channel2names.remove(channel, player.getName());
        }
        name2channels.removeAll(player.getName());
    }

}
