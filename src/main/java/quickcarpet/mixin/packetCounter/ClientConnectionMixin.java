package quickcarpet.mixin.packetCounter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.annotation.Feature;
import quickcarpet.logging.loghelpers.PacketCounter;

@Feature("packetCounter")
@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    @Inject(method = "sendImmediately", at = @At("HEAD"))
    private void countPacketOut(Packet<?> packet_1, GenericFutureListener<? extends Future<? super Void>> genericFutureListener_1, CallbackInfo ci) {
        PacketCounter.totalOut++;
    }

    @Inject(method = "channelRead0", at = @At("HEAD"))
    private void countPacketIn(ChannelHandlerContext channelHandlerContext_1, Packet<?> packet_1, CallbackInfo ci) {
        PacketCounter.totalIn++;
    }
}
