package quickcarpet.utils;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public interface IWorld {
    boolean setBlockStateWithBlockEntity(BlockPos pos, BlockState state, BlockEntity newBlockEntity, int flags);
}
