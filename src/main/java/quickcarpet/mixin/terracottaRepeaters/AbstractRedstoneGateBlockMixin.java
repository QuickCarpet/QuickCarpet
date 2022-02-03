package quickcarpet.mixin.terracottaRepeaters;

import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractRedstoneGateBlock.class)
public abstract class AbstractRedstoneGateBlockMixin {
    @Shadow protected abstract int getUpdateDelayInternal(BlockState state);

    protected int quickcarpet$getDelay(BlockState state, World world, BlockPos pos) {
        return getUpdateDelayInternal(state);
    }

    @Redirect(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/AbstractRedstoneGateBlock;getUpdateDelayInternal(Lnet/minecraft/block/BlockState;)I"))
    private int quickcarpet$terracottaRepeaters$getDelay(AbstractRedstoneGateBlock block, BlockState state, BlockState state2, ServerWorld world, BlockPos pos) {
        return quickcarpet$getDelay(state, world, pos);
    }

    @Redirect(method = "updatePowered", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/AbstractRedstoneGateBlock;getUpdateDelayInternal(Lnet/minecraft/block/BlockState;)I"))
    private int quickcarpet$terracottaRepeaters$getDelay(AbstractRedstoneGateBlock block, BlockState state, World world, BlockPos pos) {
        return quickcarpet$getDelay(state, world, pos);
    }
}
