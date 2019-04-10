package quickcarpet.patches;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TextComponent;

public class ServerPlayNetworkHandlerFake extends ServerPlayNetworkHandler
{
    public ServerPlayNetworkHandlerFake(MinecraftServer server, ClientConnection cc, ServerPlayerEntity playerIn)
    {
        super(server, cc, playerIn);
    }
    
    @Override
    public void sendPacket(final Packet<?> packetIn)
    {
    }
    
    @Override
    public void disconnect(TextComponent message)
    {
    }
}



