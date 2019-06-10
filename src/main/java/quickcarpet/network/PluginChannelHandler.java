package quickcarpet.network;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;

public interface PluginChannelHandler {
    Identifier[] getChannels();

    default boolean register(Identifier channel, ServerPlayerEntity player) {
        return true;
    }

    default void unregister(Identifier channel, ServerPlayerEntity player) {}

    void onCustomPayload(CustomPayloadC2SPacket packet, ServerPlayerEntity player);
}
