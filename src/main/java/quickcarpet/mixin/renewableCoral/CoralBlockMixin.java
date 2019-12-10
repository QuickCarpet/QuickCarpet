package quickcarpet.mixin.renewableCoral;

import net.minecraft.block.*;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.*;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import quickcarpet.annotation.Feature;
import quickcarpet.settings.Settings;
import quickcarpet.utils.extensions.ExtendedCoralFeature;

import java.util.Random;

@Feature("renewableCoral")
@Mixin(CoralBlock.class)
@Implements(@Interface(iface = Fertilizable.class, prefix = "fert$"))
public abstract class CoralBlockMixin implements Fertilizable {
    public boolean isFertilizable(BlockView var1, BlockPos var2, BlockState var3, boolean var4) {
        return Settings.renewableCoral && var3.get(CoralParentBlock.WATERLOGGED) && var1.getFluidState(var2.up()).matches(FluidTags.WATER);
    }

    public boolean canGrow(World var1, Random var2, BlockPos var3, BlockState var4) {
        return (double) var1.random.nextFloat() < 0.15D;
    }

    public void grow(World worldIn, Random random, BlockPos pos, BlockState blockUnder) {
        CoralFeature coral;
        int variant = random.nextInt(3);
        if (variant == 0)
            coral = new CoralClawFeature(DefaultFeatureConfig::deserialize);
        else if (variant == 1)
            coral = new CoralTreeFeature(DefaultFeatureConfig::deserialize);
        else
            coral = new CoralMushroomFeature(DefaultFeatureConfig::deserialize);

        MaterialColor color = blockUnder.getTopMaterialColor(worldIn, pos);
        BlockState proper_block = blockUnder;
        for (Block block : BlockTags.CORAL_BLOCKS.values()) {
            proper_block = block.getDefaultState();
            if (proper_block.getTopMaterialColor(worldIn, pos) == color) {
                break;
            }
        }
        worldIn.setBlockState(pos, Blocks.WATER.getDefaultState(), 4);

        if (!((ExtendedCoralFeature) coral).growSpecific(worldIn, random, pos, proper_block)) {
            worldIn.setBlockState(pos, blockUnder, 3);
        } else {
            if (worldIn.random.nextInt(10) == 0) {
                BlockPos randomPos = pos.add(worldIn.random.nextInt(16) - 8, worldIn.random.nextInt(8), worldIn.random.nextInt(16) - 8);
                if (BlockTags.CORAL_BLOCKS.contains(worldIn.getBlockState(randomPos).getBlock())) {
                    worldIn.setBlockState(randomPos, Blocks.WET_SPONGE.getDefaultState(), 3);
                }
            }
        }
    }
}
