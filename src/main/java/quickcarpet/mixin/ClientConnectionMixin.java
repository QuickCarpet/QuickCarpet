package quickcarpet.mixin;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.logging.loghelpers.PacketCounter;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    @Inject(
        method = "Lnet/minecraft/network/ClientConnection;sendImmediately(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V",
        at = @At("HEAD")
    )
    private void countPacketOut(Packet<?> packet_1, GenericFutureListener<? extends Future<? super Void>> genericFutureListener_1, CallbackInfo ci) {
        PacketCounter.totalOut++;
    }

    @Inject(
        method = "Lnet/minecraft/network/ClientConnection;method_10770(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V",
        at = @At("HEAD")
    )
    private void countPacketIn(ChannelHandlerContext channelHandlerContext_1, Packet<?> packet_1, CallbackInfo ci) {
        PacketCounter.totalIn++;
    }
}
