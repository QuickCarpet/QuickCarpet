package quickcarpet.patches;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.Packet;

import javax.annotation.Nullable;

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
    public boolean hasChannel() {
        return false;
    }

    @Override
    public void send(Packet<?> packet_1, @Nullable GenericFutureListener<? extends Future<? super Void>> genericFutureListener_1) {}

    @Override
    public void disableAutoRead() {}

    @Override
    public void handleDisconnection() {}
}
