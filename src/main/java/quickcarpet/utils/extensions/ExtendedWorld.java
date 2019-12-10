package quickcarpet.utils.extensions;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public interface ExtendedWorld {
    boolean setBlockStateWithBlockEntity(BlockPos pos, BlockState state, BlockEntity newBlockEntity, int flags);
}
