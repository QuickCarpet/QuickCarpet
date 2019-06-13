package quickcarpet.feature;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Random;

public class ObsidianBlock extends Block {
    public ObsidianBlock(Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return quickcarpet.settings.Settings.renewableLava;
    }

    @Override
    public void onScheduledTick(BlockState state, World world, BlockPos pos, Random random) {
        for (Direction dir : Direction.values()) {
            FluidState neighbor = world.getFluidState(pos.offset(dir));
            if (neighbor.getFluid() != Fluids.LAVA || !neighbor.isStill()) return;
        }
        if (random.nextInt(10) == 0) {
            world.setBlockState(pos, Blocks.LAVA.getDefaultState());
        }
    }
}
