package quickcarpet.mixin.updateSuppressionBlock;

import net.minecraft.block.BarrierBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BarrierBlock.class)
public class BarrierBlockMixin extends Block {
    public BarrierBlockMixin(Settings settings) {
        super(settings);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        super.neighborUpdate(state, world, pos, block, fromPos, notify);
        if (quickcarpet.settings.Settings.updateSuppressionBlock && !world.isClient) {
            throw new StackOverflowError("Update suppression block");
        }
    }
}
