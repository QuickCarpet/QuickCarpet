package quickcarpet.api.network.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import quickcarpet.api.network.PluginChannelManager;

@Environment(EnvType.CLIENT)
public interface ClientPluginChannelManager extends PluginChannelManager<ClientPluginChannelHandler> {
    boolean process(CustomPayloadS2CPacket packet, ClientPlayNetworkHandler netHandler);
    void sendRegisterPacket(ClientPlayNetworkHandler netHandler);
}
