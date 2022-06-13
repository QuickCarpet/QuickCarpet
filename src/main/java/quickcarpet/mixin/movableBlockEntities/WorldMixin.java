package quickcarpet.mixin.movableBlockEntities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import quickcarpet.utils.mixin.extensions.ExtendedWorld;
import quickcarpet.utils.mixin.extensions.ExtendedWorldChunk;

import static quickcarpet.utils.Constants.SetBlockState.*;

@Mixin(World.class)
public abstract class WorldMixin implements WorldAccess, ExtendedWorld {
    @Shadow @Final public boolean isClient;

    @Shadow public abstract void updateListeners(BlockPos var1, BlockState var2, BlockState var3, int var4);
    @Shadow public abstract void updateComparators(BlockPos blockPos_1, Block block_1);
    @Shadow public abstract void onBlockChanged(BlockPos blockPos_1, BlockState blockState_1, BlockState blockState_2);
    @Shadow public abstract WorldChunk getWorldChunk(BlockPos blockPos_1);
    @Shadow public abstract BlockState getBlockState(BlockPos blockPos_1);
    @Shadow public abstract Profiler getProfiler();
    @Shadow public abstract boolean isDebugWorld();
    @Shadow public abstract void scheduleBlockRerenderIfNeeded(BlockPos pos, BlockState old, BlockState updated);

    /**
     * @author 2No2Name
     */
    @Override
    public boolean quickcarpet$setBlockStateWithBlockEntity(BlockPos pos, BlockState state, BlockEntity newBlockEntity, int flags, int depth) {
        if (this.isOutOfHeightLimit(pos)) return false;
        if (!this.isClient && this.isDebugWorld()) return false;
        WorldChunk worldChunk = this.getWorldChunk(pos);
        Block block = state.getBlock();

        BlockState oldState;
        if (newBlockEntity != null && block instanceof BlockEntityProvider && !worldChunk.isEmpty()) {
            oldState = ((ExtendedWorldChunk) worldChunk).quickcarpet$setBlockStateWithBlockEntity(pos, state, newBlockEntity, (flags & CALL_ON_ADDED_ON_REMOVED) != 0);
        } else {
            oldState = worldChunk.setBlockState(pos, state, (flags & CALL_ON_ADDED_ON_REMOVED) != 0);
        }

        if (oldState == null) return false;
        BlockState newState = this.getBlockState(pos);

        if ((flags & SKIP_LIGHTING_UPDATES) == 0 && newState != oldState && (newState.getOpacity(this, pos) != oldState.getOpacity(this, pos) || newState.getLuminance() != oldState.getLuminance() || newState.hasSidedTransparency() || oldState.hasSidedTransparency())) {
            this.getProfiler().push("queueCheckLight");
            this.getChunkManager().getLightingProvider().checkBlock(pos);
            this.getProfiler().pop();
        }

        if (newState == state) {
            if (oldState != newState) {
                this.scheduleBlockRerenderIfNeeded(pos, oldState, newState);
            }

            if ((flags & SEND_TO_CLIENT) != 0 && (!this.isClient || (flags & NO_RERENDER) == 0) && (this.isClient || worldChunk.getLevelType() != null && worldChunk.getLevelType().isAfter(ChunkHolder.LevelType.TICKING))) {
                this.updateListeners(pos, oldState, state, flags);
            }

            if ((flags & UPDATE_NEIGHBORS) != 0) {
                this.updateNeighbors(pos, oldState.getBlock());
                if (!this.isClient && state.hasComparatorOutput()) {
                    this.updateComparators(pos, block);
                }
            }

            if ((flags & (NO_OBSERVER_UPDATE | NO_FILL_UPDATE)) == 0 && depth > 0) {
                int maskedFlags = flags & ~(UPDATE_NEIGHBORS | SKIP_DROPS);
                oldState.prepare(this, pos, maskedFlags, depth - 1);
                state.updateNeighbors(this, pos, maskedFlags, depth - 1);
                state.prepare(this, pos, maskedFlags, depth - 1);
            }

            this.onBlockChanged(pos, oldState, newState);
        }
        return true;
    }
}
