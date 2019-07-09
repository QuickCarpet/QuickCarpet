package quickcarpet.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.level.LevelGeneratorType;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.utils.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static quickcarpet.utils.Constants.SetBlockState.*;

@Mixin(World.class)
public abstract class WorldMixin implements IWorld, SpawnEntityCache {
    @Shadow @Final protected LevelProperties properties;
    @Shadow @Final private Profiler profiler;
    @Shadow @Final public boolean isClient;

    @Shadow public abstract ChunkManager getChunkManager();

    @Shadow public abstract void updateListeners(BlockPos var1, BlockState var2, BlockState var3, int var4);

    @Shadow public abstract void updateNeighbors(BlockPos blockPos_1, Block block_1);

    @Shadow public abstract void updateHorizontalAdjacent(BlockPos blockPos_1, Block block_1);

    @Shadow public abstract void onBlockChanged(BlockPos blockPos_1, BlockState blockState_1, BlockState blockState_2);

    @Shadow public abstract WorldChunk getWorldChunk(BlockPos blockPos_1);

    @Shadow public abstract BlockState getBlockState(BlockPos blockPos_1);

    @ModifyConstant(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z",
            constant = @Constant(intValue = NO_OBSERVER_UPDATE))
    private int addFillUpdatesInt(int original) {
        return NO_OBSERVER_UPDATE | NO_FILL_UPDATE;
    }

    @Inject(method = "tickBlockEntities", at = @At("HEAD"))
    private void startBlockEntities(CallbackInfo ci) {
        if (!this.isClient) {
            CarpetProfiler.endSection((World) (Object) this); // end entities
            CarpetProfiler.startSection((World) (Object) this, CarpetProfiler.SectionType.BLOCK_ENTITIES);
        }
    }

    @Inject(method = "tickBlockEntities", at = @At("TAIL"))
    private void endBlockEntities(CallbackInfo ci) {
        if (!this.isClient) {
            CarpetProfiler.endSection((World) (Object) this);
        }
    }

    @Redirect(
            method = "tickBlockEntities",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Tickable;tick()V")
    )
    private void tickBlockEntity(Tickable tickable) {
        if (!this.isClient) {
            CarpetProfiler.startBlockEntity((World) (Object) this, (BlockEntity) tickable);
            tickable.tick();
            CarpetProfiler.endBlockEntity((World) (Object) this);
        } else {
            tickable.tick();
        }
    }

    @Inject(method = "tickEntity", at = @At("HEAD"))
    private void startEntity(Consumer<Entity> tick, Entity e, CallbackInfo ci) {
        if (!this.isClient) {
            CarpetProfiler.startEntity((World) (Object) this, e);
        }
    }

    @Inject(method = "tickEntity", at = @At("TAIL"))
    private void endEntity(Consumer<Entity> tick, Entity e, CallbackInfo ci) {
        if (!this.isClient) {
            CarpetProfiler.endEntity((World) (Object) this);
        }
    }

    private final Map<EntityType<?>, Entity> CACHED_ENTITIES = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Entity> T getCachedEntity(EntityType<T> type) {
        return (T) CACHED_ENTITIES.get(type);
    }

    @Override
    public <T extends Entity> void setCachedEntity(EntityType<T> type, T entity) {
        CACHED_ENTITIES.put(type, entity);
    }

    /**
     * @author 2No2Name
     */
    public boolean setBlockStateWithBlockEntity(BlockPos blockPos_1, BlockState blockState_1, BlockEntity newBlockEntity, int int_1) {
        if (World.isHeightInvalid(blockPos_1)) {
            return false;
        } else if (!this.isClient && this.properties.getGeneratorType() == LevelGeneratorType.DEBUG_ALL_BLOCK_STATES) {
            return false;
        } else {
            WorldChunk worldChunk_1 = this.getWorldChunk(blockPos_1);
            Block block_1 = blockState_1.getBlock();

            BlockState blockState_2;
            if (newBlockEntity != null && block_1 instanceof BlockEntityProvider && !worldChunk_1.isEmpty())
                blockState_2 = ((IWorldChunk) worldChunk_1).setBlockStateWithBlockEntity(blockPos_1, blockState_1, newBlockEntity, (int_1 & CALL_ON_ADDED_ON_REMOVED) != 0);
            else
                blockState_2 = worldChunk_1.setBlockState(blockPos_1, blockState_1, (int_1 & CALL_ON_ADDED_ON_REMOVED) != 0);

            if (blockState_2 == null) {
                return false;
            } else {
                BlockState blockState_3 = this.getBlockState(blockPos_1);

                if (blockState_3 != blockState_2 && (blockState_3.getLightSubtracted((BlockView) this, blockPos_1) != blockState_2.getLightSubtracted((BlockView) this, blockPos_1) || blockState_3.getLuminance() != blockState_2.getLuminance() || blockState_3.hasSidedTransparency() || blockState_2.hasSidedTransparency())) {
                    this.profiler.push("queueCheckLight");
                    this.getChunkManager().getLightingProvider().enqueueLightUpdate(blockPos_1);
                    this.profiler.pop();
                }

                if (blockState_3 == blockState_1) {
                    if (blockState_2 != blockState_3) {
                        Reflection.scheduleBlockRender((World) (Object) this, blockPos_1, blockState_2, blockState_3);
                    }

                    if ((int_1 & SEND_TO_CLIENT) != 0 && (!this.isClient || (int_1 & NO_RERENDER) == 0) && (this.isClient || worldChunk_1.getLevelType() != null && worldChunk_1.getLevelType().isAfter(ChunkHolder.LevelType.TICKING))) {
                        this.updateListeners(blockPos_1, blockState_2, blockState_1, int_1);
                    }

                    if (!this.isClient && (int_1 & 1) != 0) {
                        this.updateNeighbors(blockPos_1, blockState_2.getBlock());
                        if (blockState_1.hasComparatorOutput()) {
                            this.updateHorizontalAdjacent(blockPos_1, block_1);
                        }
                    }

                    if ((int_1 & (NO_OBSERVER_UPDATE | NO_FILL_UPDATE)) == 0) {
                        int int_2 = int_1 & -2;
                        blockState_2.method_11637((net.minecraft.world.IWorld) this, blockPos_1, int_2);
                        blockState_1.updateNeighborStates((net.minecraft.world.IWorld) this, blockPos_1, int_2);
                        blockState_1.method_11637((net.minecraft.world.IWorld) this, blockPos_1, int_2);
                    }

                    this.onBlockChanged(blockPos_1, blockState_2, blockState_3);
                }
                return true;
            }
        }
    }
}
