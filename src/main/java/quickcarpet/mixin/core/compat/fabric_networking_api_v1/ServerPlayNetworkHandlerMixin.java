package quickcarpet.mixin.core.compat.fabric_networking_api_v1;

import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.QuickCarpetServer;

@Mixin(value = ServerPlayNetworkHandler.class, priority = 900)
public class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    @Inject(method = "onCustomPayload", at = @At("HEAD"))
    private void quickcarpet$onCustomPacket$early(CustomPayloadC2SPacket packet, CallbackInfo ci) {
        String channel = packet.getChannel().toString();
        if ("minecraft:register".equals(channel) || "minecraft:unregister".equals(channel)) {
            QuickCarpetServer.getInstance().getPluginChannelManager().process(this.player, packet);
        }
    }
}
