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
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.level.LevelGeneratorType;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import quickcarpet.annotation.Feature;
import quickcarpet.utils.Reflection;
import quickcarpet.utils.extensions.ExtendedWorld;
import quickcarpet.utils.extensions.ExtendedWorldChunk;

import static quickcarpet.utils.Constants.SetBlockState.*;

@Feature("movableBlockEntities")
@Mixin(World.class)
public abstract class WorldMixin implements ExtendedWorld {
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

    /**
     * @author 2No2Name
     */
    public boolean setBlockStateWithBlockEntity(BlockPos pos, BlockState state, BlockEntity newBlockEntity, int flags) {
        if (World.isHeightInvalid(pos)) return false;
        if (!this.isClient && this.properties.getGeneratorType() == LevelGeneratorType.DEBUG_ALL_BLOCK_STATES) return false;
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

        if (previousState != chunkState && (previousState.getOpacity((BlockView) this, pos) != chunkState.getOpacity((BlockView) this, pos) || previousState.getLuminance() != chunkState.getLuminance() || previousState.hasSidedTransparency() || chunkState.hasSidedTransparency())) {
            this.profiler.push("queueCheckLight");
            this.getChunkManager().getLightingProvider().checkBlock(pos);
            this.profiler.pop();
        }

        if (previousState == state) {
            if (chunkState != previousState) {
                Reflection.scheduleBlockRender((World) (Object) this, pos, chunkState, previousState);
            }

            if ((flags & SEND_TO_CLIENT) != 0 && (!this.isClient || (flags & NO_RERENDER) == 0) && (this.isClient || worldChunk.getLevelType() != null && worldChunk.getLevelType().isAfter(ChunkHolder.LevelType.TICKING))) {
                this.updateListeners(pos, chunkState, state, flags);
            }

            if (!this.isClient && (flags & 1) != 0) {
                this.updateNeighbors(pos, chunkState.getBlock());
                if (state.hasComparatorOutput()) {
                    this.updateHorizontalAdjacent(pos, block);
                }
            }

            if ((flags & (NO_OBSERVER_UPDATE | NO_FILL_UPDATE)) == 0) {
                int int_2 = flags & ~UPDATE_NEIGHBORS;
                chunkState.method_11637((net.minecraft.world.IWorld) this, pos, int_2);
                state.updateNeighborStates((net.minecraft.world.IWorld) this, pos, int_2);
                state.method_11637((net.minecraft.world.IWorld) this, pos, int_2);
            }

            this.onBlockChanged(pos, chunkState, previousState);
        }
        return true;
    }
}
