package quickcarpet.api.network.server;

import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import quickcarpet.api.network.PluginChannelHandler;

public interface ServerPluginChannelHandler extends PluginChannelHandler {
    default boolean register(Identifier channel, ServerPlayerEntity player) {
        return true;
    }
    default void unregister(Identifier channel, ServerPlayerEntity player) {}
    void onCustomPayload(CustomPayloadC2SPacket packet, ServerPlayerEntity player);
}
