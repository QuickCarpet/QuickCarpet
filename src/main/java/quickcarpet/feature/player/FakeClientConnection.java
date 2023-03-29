package quickcarpet.feature.player;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;

public class FakeClientConnection extends ClientConnection {
    public boolean open = true;

    public FakeClientConnection(NetworkSide side) {
        super(side);
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public boolean isChannelAbsent() {
        return false;
    }

    @Override
    public void send(Packet<?> packet, @Nullable PacketCallbacks callbacks) {}

    @Override
    public void disableAutoRead() {}

    @Override
    public void handleDisconnection() {}
}
