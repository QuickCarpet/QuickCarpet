package quickcarpet.mixin.fillUpdates;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.mixin.accessor.UpgradeDataAccessor;
import quickcarpet.settings.Settings;

import static quickcarpet.utils.Constants.SetBlockState.DEFAULT;
import static quickcarpet.utils.Constants.SetBlockState.NO_FILL_UPDATE;

@Mixin(BlockStateArgument.class)
public class BlockStateArgumentMixin {
    private static final Direction[] FACINGS = Direction.values();

    @Redirect(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;postProcessState(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
    private BlockState quickcarpet$fillUpdates$postProcessState(BlockState state, WorldAccess world, BlockPos pos, ServerWorld serverWorld, BlockPos blockPos, int flags) {
        if ((flags & NO_FILL_UPDATE) == 0) return Block.postProcessState(state, world, pos);
        if (!Settings.fillUpdatesPostProcessing) return state;
        BlockPos.Mutable neighbor = new BlockPos.Mutable();
        for (Direction dir : FACINGS) {
            neighbor.set(pos, dir);
            state = UpgradeDataAccessor.invokeApplyAdjacentBlock(state, dir, world, pos, neighbor);
        }
        BlockState previous = world.getBlockState(pos);
        world.setBlockState(pos, state, flags);
        for (Direction dir : FACINGS) {
            neighbor.set(pos, dir);
            BlockState neighborState = world.getBlockState(neighbor);
            BlockState updatedNeighbor = UpgradeDataAccessor.invokeApplyAdjacentBlock(neighborState, dir.getOpposite(), world, neighbor, pos);
            if (updatedNeighbor != neighborState) {
                world.setBlockState(neighbor, updatedNeighbor, DEFAULT | NO_FILL_UPDATE);
            }
        }
        world.setBlockState(pos, previous, flags);
        return state;
    }
}
