package quickcarpet.utils.mixin.extensions;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public interface ExtendedWorldChunk {
    BlockState quickcarpet$setBlockStateWithBlockEntity(BlockPos pos, BlockState newBlockState, BlockEntity newBlockEntity, boolean callListeners);
}
