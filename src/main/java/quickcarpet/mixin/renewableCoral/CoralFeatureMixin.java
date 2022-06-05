package quickcarpet.mixin.renewableCoral;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.CoralFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import quickcarpet.utils.extensions.ExtendedCoralFeature;

@Mixin(CoralFeature.class)
public abstract class CoralFeatureMixin implements ExtendedCoralFeature {
    @Shadow
    protected abstract boolean generateCoral(WorldAccess var1, Random var2, BlockPos var3, BlockState var4);

    @Override
    public boolean quickcarpet$growSpecific(World worldIn, Random random, BlockPos pos, BlockState blockUnder) {
        return generateCoral(worldIn, random, pos, blockUnder);
    }
}
