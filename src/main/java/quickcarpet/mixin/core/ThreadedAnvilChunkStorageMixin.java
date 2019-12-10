package quickcarpet.mixin.core;

import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.annotation.Feature;
import quickcarpet.network.channels.StructureChannel;

@Feature("structureChannel")
@Mixin(ThreadedAnvilChunkStorage.class)
public class ThreadedAnvilChunkStorageMixin {
    @Inject(method = "sendWatchPackets", at = @At("HEAD"))
    private void recordChunkSent(ServerPlayerEntity player, ChunkPos pos, Packet<?>[] packets, boolean b1, boolean b2, CallbackInfo ci) {
        StructureChannel.instance.recordChunkSent(player, pos);
    }
}
