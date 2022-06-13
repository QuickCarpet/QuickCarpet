package quickcarpet.utils.mixin.extensions;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public interface ExtendedCoralFeature {
    boolean quickcarpet$growSpecific(World worldIn, Random random, BlockPos pos, BlockState blockUnder);
}
