package quickcarpet.mixin.terracottaRepeaters;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import quickcarpet.utils.CarpetRegistry;

@Mixin(RepeaterBlock.class)
public abstract class RepeaterBlockMixin extends AbstractRedstoneGateBlockMixin {
    @Shadow @Final public static IntProperty DELAY;

    @Override
    protected int getDelay(BlockState state, World world, BlockPos pos) {
        int delay = 2;
        if (quickcarpet.settings.Settings.terracottaRepeaters) {
            BlockState stateBelow = world.getBlockState(pos.down());
            Block blockBelow = stateBelow.getBlock();
            delay = CarpetRegistry.TERRACOTTA_BLOCKS.getOrDefault(blockBelow, delay);
            if (delay == 0) delay = 100;
        }
        return state.get(DELAY) * delay;
    }
}
