package quickcarpet.mixin.fillUpdates;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static quickcarpet.utils.Constants.SetBlockState.NO_FILL_UPDATE;

@Mixin(BlockStateArgument.class)
public class BlockStateArgumentMixin {
    @Redirect(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;postProcessState(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
    private BlockState postProcessState(BlockState state, WorldAccess world, BlockPos pos, ServerWorld serverWorld, BlockPos blockPos, int flags) {
        return (flags & NO_FILL_UPDATE) != 0 ? state : Block.postProcessState(state, world, pos);
    }
}
