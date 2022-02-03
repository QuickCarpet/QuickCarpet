package quickcarpet.utils.extensions;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public interface ExtendedWorldChunkFillUpdates {
    @Nullable
    BlockState quickcarpet$setBlockStateWithoutUpdates(BlockPos pos, BlockState state, boolean moved);
}
