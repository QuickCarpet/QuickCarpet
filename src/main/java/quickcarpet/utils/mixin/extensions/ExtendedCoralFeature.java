package quickcarpet.utils.mixin.extensions;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public interface ExtendedCoralFeature {
    boolean quickcarpet$growSpecific(World worldIn, Random random, BlockPos pos, BlockState blockUnder);
}
