package quickcarpet.mixin.accessor;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.ServerNetworkIo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ServerNetworkIo.class)
public interface ServerNetworkIoAccessor {
    @Accessor("connections")
    List<ClientConnection> getConnections();
}
