package quickcarpet.mixin.accessor;

import net.minecraft.client.network.packet.KeepAliveS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeepAliveS2CPacket.class)
public interface KeepAliveS2CPacketAccessor {
    @Accessor("id")
    long getId();
}
