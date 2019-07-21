package quickcarpet.patches;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class FakeServerPlayNetworkHandler extends ServerPlayNetworkHandler {
    public FakeServerPlayNetworkHandler(MinecraftServer server, ClientConnection cc, ServerPlayerEntity playerIn) {
        super(server, cc, playerIn);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void sendPacket(final Packet<?> packetIn) {}

    @Override
    public void disconnect(Text message) {}

    @Override
    public void onDisconnected(Text text_1) {
        super.onDisconnected(text_1);
        ((FakeClientConnection) this.getConnection()).open = false;
    }
}



