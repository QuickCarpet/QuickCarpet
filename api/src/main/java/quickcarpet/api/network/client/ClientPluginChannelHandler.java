package quickcarpet.api.network.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import quickcarpet.api.network.PluginChannelHandler;

@Environment(EnvType.CLIENT)
public interface ClientPluginChannelHandler extends PluginChannelHandler {
    void onCustomPayload(CustomPayloadS2CPacket packet, ClientPlayPacketListener netHandler);
}
