package quickcarpet.mixin;

import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.utils.CarpetProfiler;

@Mixin(ServerChunkManager.class)
public abstract class MixinServerChunkManager {

    /*
    @Shadow @Final private ServerWorld world;

    @Redirect(method = "doMobSpawning", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;method_18203(Lnet/minecraft/world/chunk/WorldChunk;I)V"))
    private void ifMethod_18203(ServerWorld serverWorld, WorldChunk worldChunk_1, int int_1){

    }

    @Inject(method = "doMobSpawning", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/server/world/ServerWorld;method_18203(Lnet/minecraft/world/chunk/WorldChunk;I)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void processBlocks(CallbackInfo ci, long  long_1, long  long_2, LevelProperties levelProperties_1, boolean  boolean_1,
                               boolean  boolean_2, int  int_1, int  int_2, BlockPos blockPos_1, boolean  boolean_3, EntityCategory[]  entityCategorys_1,
                               Object2IntMap object2IntMap_1, ObjectBidirectionalIterator objectBidirectionalIterator_1, ChunkHolder chunkHolder_1,
                               Long2ObjectMap.Entry  long2ObjectMap$Entry_1, WorldChunk  worldChunk_1){
        if (!TickSpeed.process_entities) {
            this.world.getProfiler().pop();
        }
        else
        {
            this.world.method_18203(worldChunk_1, int_2);
        }
    }
    */

    @Shadow @Final private ServerWorld world;

    @Inject(
        method = "tickChunks",
        at = @At(value = "CONSTANT", args = "stringValue=spawner")
    )
    private void startSpawning(CallbackInfo ci) {
        CarpetProfiler.startSection(this.world, CarpetProfiler.SectionType.SPAWNING);
    }

    @Inject(
        method = "tickChunks",
        slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=spawner")),
        at = @At(value = "INVOKE", target="Lnet/minecraft/util/profiler/Profiler;pop()V", ordinal = 0)
    )
    private void endSpawning(CallbackInfo ci) {
        CarpetProfiler.endSection(this.world);
    }
}
