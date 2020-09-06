package quickcarpet.mixin.fillUpdates;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.annotation.Feature;
import quickcarpet.utils.extensions.ExtendedWorldChunkFillUpdates;

import static quickcarpet.utils.Constants.SetBlockState.NO_FILL_UPDATE;
import static quickcarpet.utils.Constants.SetBlockState.NO_OBSERVER_UPDATE;

@Feature("fillUpdates")
@Mixin(World.class)
public class WorldMixin {
    @ModifyConstant(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z",
            constant = @Constant(intValue = NO_OBSERVER_UPDATE))
    private int addFillUpdatesInt(int original) {
        return NO_OBSERVER_UPDATE | NO_FILL_UPDATE;
    }

    @Redirect(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;"))
    private BlockState setBlockStateInChunk(WorldChunk worldChunk, BlockPos pos, BlockState state, boolean moved, BlockPos pos1, BlockState state1, int flags) {
        if ((flags & NO_FILL_UPDATE) != 0) {
            return ((ExtendedWorldChunkFillUpdates) worldChunk).setBlockStateWithoutUpdates(pos, state, moved);
        }
        return worldChunk.setBlockState(pos, state, moved);
    }
}
