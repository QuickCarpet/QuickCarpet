package quickcarpet.utils.extensions;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public interface ExtendedWorld {
    default boolean setBlockStateWithBlockEntity(BlockPos pos, BlockState state, BlockEntity newBlockEntity, int flags) {
        return setBlockStateWithBlockEntity(pos, state, newBlockEntity, flags,512);
    }

    boolean setBlockStateWithBlockEntity(BlockPos pos, BlockState state, BlockEntity newBlockEntity, int flags, int depth);
}
