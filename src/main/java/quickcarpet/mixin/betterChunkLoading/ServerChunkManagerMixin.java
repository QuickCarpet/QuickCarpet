package quickcarpet.mixin.betterChunkLoading;

import com.mojang.datafixers.util.Either;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.settings.Settings;

import java.util.concurrent.CompletableFuture;

@Mixin(ServerChunkManager.class)
public abstract class ServerChunkManagerMixin {
    @Redirect(method = "isTickingFutureReady", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkHolder;getTickingFuture()Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>> quickcarpet$betterChunkLoading$tickAllBlocks(ChunkHolder chunkHolder) {
        if (Settings.betterChunkLoading) return chunkHolder.getAccessibleFuture();
        return chunkHolder.getTickingFuture();
    }
}
