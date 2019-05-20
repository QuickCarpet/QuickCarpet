package quickcarpet.utils;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public interface IWorld
{
    boolean setBlockStateWithBlockEntity(BlockPos blockPos_1, BlockState blockState_1, BlockEntity newBlockEntity, int int_1);
}
