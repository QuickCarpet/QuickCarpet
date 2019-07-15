package quickcarpet.patches;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;

public class FakeClientConnection extends ClientConnection {
    public FakeClientConnection(NetworkSide side) {
        super(side);
    }

    @Override
    public void disableAutoRead() {}

    @Override
    public void handleDisconnection() {}
}
