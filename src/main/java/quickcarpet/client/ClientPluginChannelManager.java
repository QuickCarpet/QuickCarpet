package quickcarpet.client;

import com.google.common.base.Charsets;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;
import quickcarpet.api.network.client.ClientPluginChannelHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class ClientPluginChannelManager implements quickcarpet.api.network.client.ClientPluginChannelManager {
    public static final ClientPluginChannelManager INSTANCE = new ClientPluginChannelManager();
    public final Map<Identifier, ClientPluginChannelHandler> HANDLERS = new HashMap<>();

    private ClientPluginChannelManager() {}

    @Override
    public void register(ClientPluginChannelHandler handler) {
        for (Identifier channel : handler.getChannels()) {
            HANDLERS.put(channel, handler);
        }
    }

    @Override
    public void unregister(ClientPluginChannelHandler handler) {
        for (Identifier channel : handler.getChannels()) {
            HANDLERS.remove(channel);
        }
    }

    @Override
    public boolean process(CustomPayloadS2CPacket packet, ClientPlayNetworkHandler netHandler) {
        Identifier channel = packet.getChannel();
        ClientPluginChannelHandler handler = HANDLERS.get(channel);
        if (handler != null) {
            handler.onCustomPayload(packet, netHandler);
            return true;
        }
        return false;
    }

    @Override
    public void sendRegisterPacket(ClientPlayNetworkHandler netHandler) {
        System.out.println("sending register for " + HANDLERS.keySet());
        byte[] bytes = HANDLERS.keySet().stream().map(Identifier::toString).collect(Collectors.joining("\0")).getBytes(Charsets.UTF_8);
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer(bytes.length));
        buf.writeBytes(bytes);
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(new Identifier("register"), buf);
        netHandler.sendPacket(packet);
    }
}
