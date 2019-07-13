package quickcarpet.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.CoralFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import quickcarpet.annotation.Feature;
import quickcarpet.utils.ICoralFeature;

import java.util.Random;

@Feature("renewableCoral")
@Mixin(CoralFeature.class)
public abstract class CoralFeatureMixin implements ICoralFeature
{
    @Shadow protected abstract boolean spawnCoral(IWorld var1, Random var2, BlockPos var3, BlockState var4);

    @Override
    public boolean growSpecific(World worldIn, Random random, BlockPos pos, BlockState blockUnder)
    {
        return spawnCoral(worldIn, random, pos, blockUnder);
    }
}
