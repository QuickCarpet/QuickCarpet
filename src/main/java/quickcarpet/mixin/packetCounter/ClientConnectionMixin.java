package quickcarpet.mixin.packetCounter;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.logging.source.PacketCounterLoggerSource;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    @Inject(method = "sendImmediately", at = @At("HEAD"))
    private void quickcarpet$packetCounter$out(Packet<?> packet, PacketCallbacks callbacks, CallbackInfo ci) {
        PacketCounterLoggerSource.out();
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"))
    private void quickcarpet$packetCounter$in(ChannelHandlerContext channelHandlerContext_1, Packet<?> packet_1, CallbackInfo ci) {
        PacketCounterLoggerSource.in();
    }
}
