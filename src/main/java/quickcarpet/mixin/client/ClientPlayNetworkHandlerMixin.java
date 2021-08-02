package quickcarpet.mixin.client;

import com.google.common.collect.Multimap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeTagsS2CPacket;
import net.minecraft.tag.TagManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.QuickCarpetClient;
import quickcarpet.client.ClientPluginChannelManager;
import quickcarpet.utils.CarpetRegistry;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin implements ClientPlayPacketListener {
    @Inject(method = "onGameJoin", at = @At("RETURN"))
    private void onJoinServer(CallbackInfo ci) {
        QuickCarpetClient.getInstance().onJoinServer();
    }

    @Inject(method = "clearWorld", at = @At("HEAD"))
    private void onLeaveServer(CallbackInfo ci) {
        QuickCarpetClient.getInstance().onLeaveServer();
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

    @Inject(method = "onSynchronizeTags", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Multimap;isEmpty()Z", remap = false), locals = LocalCapture.CAPTURE_FAILHARD)
    private void ignoreMissingCarpetTags(SynchronizeTagsS2CPacket packet, CallbackInfo ci, TagManager manager, Multimap<RegistryKey<? extends Registry<?>>, Identifier> map) {
        map.get(Registry.BLOCK_KEY).removeAll(CarpetRegistry.CARPET_BLOCK_TAGS);
    }
}
