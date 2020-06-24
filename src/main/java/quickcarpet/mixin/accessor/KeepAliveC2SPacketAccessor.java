package quickcarpet.mixin.accessor;

import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeepAliveC2SPacket.class)
public interface KeepAliveC2SPacketAccessor {
    @Accessor("id")
    void setId(long id);
}
