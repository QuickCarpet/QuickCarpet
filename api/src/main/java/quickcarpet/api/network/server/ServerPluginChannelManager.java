package quickcarpet.api.network.server;

import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import quickcarpet.api.network.PluginChannelManager;

public interface ServerPluginChannelManager extends PluginChannelManager<ServerPluginChannelHandler> {
    void process(ServerPlayerEntity player, CustomPayloadC2SPacket packet);
    void onPlayerConnect(ServerPlayerEntity player);
    void onPlayerDisconnect(ServerPlayerEntity player);
}
