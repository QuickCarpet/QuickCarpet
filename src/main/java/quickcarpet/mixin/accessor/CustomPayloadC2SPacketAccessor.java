package quickcarpet.mixin.accessor;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import quickcarpet.api.annotation.Feature;

@Feature("logger.hud")
@Mixin(CustomPayloadC2SPacket.class)
public interface CustomPayloadC2SPacketAccessor {
    @Accessor("channel")
    Identifier getChannel();

    @Accessor("data")
    PacketByteBuf getData();
}
