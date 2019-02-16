package quickcarpet.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import net.minecraft.entity.EntityCategory;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.helper.TickSpeed;

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
}
