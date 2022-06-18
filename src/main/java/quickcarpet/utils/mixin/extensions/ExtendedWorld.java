package quickcarpet.utils.mixin.extensions;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public interface ExtendedWorld {
    default boolean quickcarpet$setBlockStateWithBlockEntity(BlockPos pos, BlockState state, BlockEntity newBlockEntity, int flags) {
        return quickcarpet$setBlockStateWithBlockEntity(pos, state, newBlockEntity, flags,512);
    }

    boolean quickcarpet$setBlockStateWithBlockEntity(BlockPos pos, BlockState state, BlockEntity newBlockEntity, int flags, int depth);
}
