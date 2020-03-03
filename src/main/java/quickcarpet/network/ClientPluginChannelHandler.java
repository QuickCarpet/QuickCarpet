package quickcarpet.network;

import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;

public interface ClientPluginChannelHandler {
    Identifier[] getChannels();
    void onCustomPayload(CustomPayloadS2CPacket packet, ClientPlayPacketListener netHandler);
}
