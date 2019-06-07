package quickcarpet.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.entity.EntityCategory;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.utils.SpawnTracker;

@Mixin(ServerChunkManager.class)
public abstract class ServerChunkManagerMixin {

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

    @Shadow @Final private ChunkTicketManager ticketManager;

    @Redirect(
        method = "tickChunks",
        at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Object2IntMap;getInt(Ljava/lang/Object;)I"),
        require = 0
    )
    private int onMobcapCheckTickChunks(Object2IntMap mobcaps, Object key) {
        return onMobcapCheck(mobcaps, key);
    }

    @Redirect(
        method = "method_20801",
        at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Object2IntMap;getInt(Ljava/lang/Object;)I"),
        require = 0
    )
    private int onMobcapCheckLambda(Object2IntMap mobcaps, Object key) {
        return onMobcapCheck(mobcaps, key);
    }

    private int onMobcapCheck(Object2IntMap mobcaps, Object key) {
        EntityCategory category = (EntityCategory) key;
        int levelCount = this.ticketManager.getLevelCount();
        int cap = category.getSpawnCap() * levelCount / (17 * 17);
        int mobsPresent = mobcaps.getInt(key);
        SpawnTracker.registerMobcapStatus(this.world.getDimension().getType(), category, mobsPresent > cap);
        return mobsPresent;
    }
}
