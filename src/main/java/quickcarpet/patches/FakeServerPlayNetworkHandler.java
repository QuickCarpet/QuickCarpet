package quickcarpet.patches;

import net.minecraft.client.network.packet.KeepAliveS2CPacket;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.KeepAliveC2SPacket;
import net.minecraft.text.Text;
import quickcarpet.mixin.accessor.KeepAliveC2SPacketAccessor;
import quickcarpet.mixin.accessor.KeepAliveS2CPacketAccessor;

public class FakeServerPlayNetworkHandler extends ServerPlayNetworkHandler {
    public FakeServerPlayNetworkHandler(MinecraftServer server, ClientConnection cc, ServerPlayerEntity playerIn) {
        super(server, cc, playerIn);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void sendPacket(final Packet<?> packet) {
        if (packet instanceof KeepAliveS2CPacket) {
            KeepAliveS2CPacket ping = (KeepAliveS2CPacket) packet;
            KeepAliveC2SPacket pong = new KeepAliveC2SPacket();
            ((KeepAliveC2SPacketAccessor) pong).setId(((KeepAliveS2CPacketAccessor) ping).getId());
            this.onKeepAlive(pong);
        }
    }

    @Override
    public void disconnect(Text message) {
        player.kill();
    }

    @Override
    public void onDisconnected(Text text_1) {
        super.onDisconnected(text_1);
        ((FakeClientConnection) this.getConnection()).open = false;
    }
}



