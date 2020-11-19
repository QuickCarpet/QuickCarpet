package quickcarpet.mixin.piston;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import quickcarpet.api.annotation.Feature;
import quickcarpet.utils.extensions.ExtendedWorld;
import quickcarpet.utils.extensions.ExtendedWorldChunk;

import static quickcarpet.utils.Constants.SetBlockState.*;

@Feature("movableBlockEntities")
@Mixin(World.class)
public abstract class WorldMixin implements ExtendedWorld {
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
    public boolean setBlockStateWithBlockEntity(BlockPos pos, BlockState state, BlockEntity newBlockEntity, int flags, int depth) {
        if (World.isInBuildLimit(pos)) return false;
        if (!this.isClient && this.isDebugWorld()) return false;
        WorldChunk worldChunk = this.getWorldChunk(pos);
        Block block = state.getBlock();

        BlockState chunkState;
        if (newBlockEntity != null && block instanceof BlockEntityProvider && !worldChunk.isEmpty()) {
            chunkState = ((ExtendedWorldChunk) worldChunk).setBlockStateWithBlockEntity(pos, state, newBlockEntity, (flags & CALL_ON_ADDED_ON_REMOVED) != 0);
        } else {
            chunkState = worldChunk.setBlockState(pos, state, (flags & CALL_ON_ADDED_ON_REMOVED) != 0);
        }

        if (chunkState == null) return false;
        BlockState previousState = this.getBlockState(pos);

        if ((flags & CHECK_LIGHT) != 0 && previousState != chunkState && (previousState.getOpacity((BlockView) this, pos) != chunkState.getOpacity((BlockView) this, pos) || previousState.getLuminance() != chunkState.getLuminance() || previousState.hasSidedTransparency() || chunkState.hasSidedTransparency())) {
            this.getProfiler().push("queueCheckLight");
            ((WorldAccess) this).getChunkManager().getLightingProvider().checkBlock(pos);
            this.getProfiler().pop();
        }

        if (previousState == state) {
            if (chunkState != previousState) {
                this.scheduleBlockRerenderIfNeeded(pos, chunkState, previousState);
            }

            if ((flags & SEND_TO_CLIENT) != 0 && (!this.isClient || (flags & NO_RERENDER) == 0) && (this.isClient || worldChunk.getLevelType() != null && worldChunk.getLevelType().isAfter(ChunkHolder.LevelType.TICKING))) {
                this.updateListeners(pos, chunkState, state, flags);
            }

            if ((flags & UPDATE_NEIGHBORS) != 0) {
                ((World) (Object) this).updateNeighbors(pos, chunkState.getBlock());
                if (!this.isClient && state.hasComparatorOutput()) {
                    this.updateComparators(pos, block);
                }
            }

            if ((flags & (NO_OBSERVER_UPDATE | NO_FILL_UPDATE)) == 0 && depth > 0) {
                int maskedFlags = flags & ~(UPDATE_NEIGHBORS | FLAG_32);
                chunkState.prepare((WorldAccess) this, pos, maskedFlags, depth - 1);
                state.updateNeighbors((WorldAccess) this, pos, maskedFlags, depth - 1);
                state.prepare((WorldAccess) this, pos, maskedFlags, depth - 1);
            }

            this.onBlockChanged(pos, chunkState, previousState);
        }
        return true;
    }
}
