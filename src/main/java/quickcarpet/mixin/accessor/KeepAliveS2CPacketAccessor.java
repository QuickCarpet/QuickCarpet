package quickcarpet.mixin.accessor;

import net.minecraft.network.packet.s2c.play.KeepAliveS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeepAliveS2CPacket.class)
public interface KeepAliveS2CPacketAccessor {
    @Accessor("id")
    long getId();
}
