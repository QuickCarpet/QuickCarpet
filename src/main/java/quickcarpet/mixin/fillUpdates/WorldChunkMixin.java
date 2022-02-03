package quickcarpet.mixin.fillUpdates;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.utils.extensions.ExtendedWorldChunkFillUpdates;

import javax.annotation.Nullable;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin implements ExtendedWorldChunkFillUpdates {
    @Shadow @Nullable public abstract BlockState setBlockState(BlockPos pos, BlockState state, boolean moved);

    private boolean fillUpdates = true;

    @Override
    @Nullable
    public BlockState quickcarpet$setBlockStateWithoutUpdates(BlockPos pos, BlockState state, boolean moved) {
        try {
            fillUpdates = false;
            return setBlockState(pos, state, moved);
        } finally {
            fillUpdates = true;
        }
    }

    @Redirect(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onBlockAdded(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)V"))
    private void quickcarpet$fillUpdates$onAdded(BlockState blockState, World world, BlockPos pos, BlockState state, boolean notify) {
        if (fillUpdates) blockState.onBlockAdded(world, pos, state, notify);
    }

    @Redirect(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onStateReplaced(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)V"))
    private void quickcarpet$fillUpdates$onReplaced(BlockState oldState, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (fillUpdates) {
            oldState.onStateReplaced(world, pos, newState, moved);
        } else {
            if (oldState.hasBlockEntity() && !oldState.isOf(newState.getBlock())) {
                world.removeBlockEntity(pos);
            }
        }
    }
}
