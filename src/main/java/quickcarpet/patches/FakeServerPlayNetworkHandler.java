package quickcarpet.patches;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import net.minecraft.network.packet.s2c.play.KeepAliveS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
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
        if (packet instanceof KeepAliveS2CPacket ping) {
            this.onKeepAlive(new KeepAliveC2SPacket(((KeepAliveS2CPacketAccessor) ping).getId()));
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



