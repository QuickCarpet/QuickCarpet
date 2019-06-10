package quickcarpet.patches;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;

public class ClientConnectionFake extends ClientConnection {
    public ClientConnectionFake(NetworkSide p) {
        super(p);
    }

    @Override
    public void disableAutoRead() {}

    @Override
    public void handleDisconnection() {}
}
