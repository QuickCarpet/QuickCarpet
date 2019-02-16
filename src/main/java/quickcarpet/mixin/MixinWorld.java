package quickcarpet.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.level.LevelGeneratorType;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(World.class)
public abstract class MixinWorld {

    @Shadow
    public static boolean isHeightInvalid(BlockPos blockPos_1) {
        return false;
    }

    @Shadow @Final public boolean isClient;

    @Shadow @Final protected LevelProperties properties;

    @Shadow public abstract WorldChunk getWorldChunk(BlockPos blockPos_1);

    @Shadow public abstract BlockState getBlockState(BlockPos blockPos_1);

    @Shadow @Final private Profiler profiler;

    @Shadow public abstract ChunkManager getChunkManager();

    @Shadow public abstract void scheduleBlockRender(BlockPos blockPos_1);

    @Shadow public abstract void updateListeners(BlockPos var1, BlockState var2, BlockState var3, int var4);

    @Shadow public abstract void updateNeighbors(BlockPos blockPos_1, Block block_1);

    @Shadow public abstract void updateHorizontalAdjacent(BlockPos blockPos_1, Block block_1);

    /**
     * @author DeadlyMC
     * @reason No injection points
     */
    @Overwrite
    public boolean setBlockState(BlockPos blockPos_1, BlockState blockState_1, int int_1) {
        if (isHeightInvalid(blockPos_1)) {
            return false;
        } else if (!this.isClient && this.properties.getGeneratorType() == LevelGeneratorType.DEBUG_ALL_BLOCK_STATES) {
            return false;
        } else {
            WorldChunk worldChunk_1 = this.getWorldChunk(blockPos_1);
            Block block_1 = blockState_1.getBlock();
            BlockState blockState_2 = worldChunk_1.setBlockState(blockPos_1, blockState_1, (int_1 & 64) != 0);
            if (blockState_2 == null) {
                return false;
            } else {
                BlockState blockState_3 = this.getBlockState(blockPos_1);
                if (blockState_3 != blockState_2 && (blockState_3.getLightSubtracted((World)(Object)this, blockPos_1) != blockState_2.getLightSubtracted((World)(Object)this, blockPos_1) || blockState_3.getLuminance() != blockState_2.getLuminance() || blockState_3.method_16386() || blockState_2.method_16386())) {
                    this.profiler.push("queueCheckLight");
                    this.getChunkManager().getLightingProvider().enqueueLightUpdate(blockPos_1);
                    this.profiler.pop();
                }

                if (blockState_3 == blockState_1) {
                    if (blockState_2 != blockState_3) {
                        this.scheduleBlockRender(blockPos_1);
                    }

                    if ((int_1 & 2) != 0 && (!this.isClient || (int_1 & 4) == 0) && (this.isClient || worldChunk_1.method_12225() != null && worldChunk_1.method_12225().isAfter(ChunkHolder.LevelType.TICKING))) {
                        this.updateListeners(blockPos_1, blockState_2, blockState_1, int_1);
                    }

                    if (!this.isClient && (int_1 & 1) != 0) {
                        this.updateNeighbors(blockPos_1, blockState_2.getBlock());
                        if (blockState_1.hasComparatorOutput()) {
                            this.updateHorizontalAdjacent(blockPos_1, block_1);
                        }
                    }

                    // fillUpdates added case
                    // if ((int_1 & 16) == 0)
                    if ((int_1 & 16) == 0 && ((int_1 & 1024) == 0)) {
                        int int_2 = int_1 & -2;
                        blockState_2.method_11637((World)(Object)this, blockPos_1, int_2);
                        blockState_1.updateNeighborStates((World)(Object)this, blockPos_1, int_2);
                        blockState_1.method_11637((World)(Object)this, blockPos_1, int_2);
                    }
                }

                return true;
            }
        }
    }

}
