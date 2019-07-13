package quickcarpet.mixin.client;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.QuickCarpet;
import quickcarpet.annotation.Feature;
import quickcarpet.client.ClientPluginChannelManager;

@Feature("core")
@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin implements ClientPlayPacketListener {
    @Inject(method = "onGameJoin", at = @At("RETURN"))
    private void onJoinServer(CallbackInfo ci) {
        QuickCarpet.getInstance().client.onJoinServer();
    }

    @Inject(method = "clearWorld", at = @At("HEAD"))
    private void onLeaveServer(CallbackInfo ci) {
        QuickCarpet.getInstance().client.onLeaveServer();
    }

    @Inject(method = "onCustomPayload", at = @At(value = "CONSTANT", args = "stringValue=Unknown custom packed identifier: {}"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT, require = 0)
    private void onCustomPayloadNotFound(CustomPayloadS2CPacket packet, CallbackInfo info, Identifier id, PacketByteBuf buf) {
        String channel = packet.getChannel().toString();
        if ("minecraft:register".equals(channel) || "minecraft:unregister".equals(channel)) {
            if (buf.refCnt() > 0) {
                buf.release();
            }

            info.cancel();
        }
    }

    @Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
    private void onCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo info) {
        if (ClientPluginChannelManager.INSTANCE.process(packet, (ClientPlayNetworkHandler) (Object) this)) {
            info.cancel();
        }
    }
}
