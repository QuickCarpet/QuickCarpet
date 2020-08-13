package quickcarpet.mixin.betterChunkLoading;

import com.mojang.datafixers.util.Either;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.settings.Settings;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Mixin(ServerChunkManager.class)
public abstract class ServerChunkManagerMixin {
    @Shadow protected abstract boolean isFutureReady(long pos, Function<ChunkHolder, CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>>> futureFunction);

    @Redirect(method = "shouldTickBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkManager;isFutureReady(JLjava/util/function/Function;)Z"))
    private boolean tickAllBlocks(ServerChunkManager serverChunkManager, long pos, Function<ChunkHolder, CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>>> futureFunction) {
        if (Settings.betterChunkLoading) return isFutureReady(pos, ChunkHolder::getAccessibleFuture);
        return isFutureReady(pos, futureFunction);
    }
}
