package quickcarpet.utils.extensions;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public interface ExtendedWorldChunk {
    BlockState setBlockStateWithBlockEntity(BlockPos pos, BlockState newBlockState, BlockEntity newBlockEntity, boolean callListeners);
}
