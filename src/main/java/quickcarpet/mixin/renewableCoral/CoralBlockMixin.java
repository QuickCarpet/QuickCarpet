package quickcarpet.mixin.renewableCoral;

import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.CoralFeature;
import net.minecraft.world.gen.feature.Feature;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import quickcarpet.settings.Settings;
import quickcarpet.utils.extensions.ExtendedCoralFeature;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Mixin(CoralBlock.class)
@Implements(@Interface(iface = Fertilizable.class, prefix = "fert$"))
public abstract class CoralBlockMixin implements Fertilizable {

    public boolean isFertilizable(BlockView var1, BlockPos var2, BlockState var3, boolean var4) {
        return Settings.renewableCoral && var3.get(CoralParentBlock.WATERLOGGED) && var1.getFluidState(var2.up()).isIn(FluidTags.WATER);
    }

    public boolean canGrow(World var1, Random var2, BlockPos var3, BlockState var4) {
        return (double) var1.random.nextFloat() < 0.15D;
    }

    @Override
    public void grow(ServerWorld worldIn, Random random, BlockPos pos, BlockState blockUnder) {
        // can't be a static final field because of bootstap order (this would load features from blocks)
        List<CoralFeature> FEATURES = Arrays.asList((CoralFeature) Feature.CORAL_CLAW, (CoralFeature) Feature.CORAL_TREE, (CoralFeature) Feature.CORAL_MUSHROOM);
        CoralFeature coral = FEATURES.get(random.nextInt(FEATURES.size()));
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
