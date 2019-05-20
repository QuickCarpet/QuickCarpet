package quickcarpet.utils;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public interface IWorldChunk
{
    BlockState setBlockStateWithBlockEntity(BlockPos blockPos_1, BlockState newBlockState, BlockEntity newBlockEntity, boolean boolean_1);
}
