package quickcarpet.mixin.renewableCoral;

import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryEntryList;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.CoralFeature;
import net.minecraft.world.gen.feature.Feature;
import org.spongepowered.asm.mixin.Mixin;
import quickcarpet.settings.Settings;
import quickcarpet.utils.mixin.extensions.ExtendedCoralFeature;

import java.util.List;

@Mixin(CoralBlock.class)
public abstract class CoralBlockMixin implements Fertilizable {
    @Override
    public boolean isFertilizable(BlockView var1, BlockPos var2, BlockState var3, boolean var4) {
        return Settings.renewableCoral && var3.get(CoralParentBlock.WATERLOGGED) && var1.getFluidState(var2.up()).isIn(FluidTags.WATER);
    }

    @Override
    public boolean canGrow(World var1, Random var2, BlockPos var3, BlockState var4) {
        return var1.random.nextFloat() < 0.30;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState blockUnder) {
        // can't be a static final field because of bootstrap order (this would load features from blocks)
        List<CoralFeature> features = List.of((CoralFeature) Feature.CORAL_CLAW, (CoralFeature) Feature.CORAL_TREE, (CoralFeature) Feature.CORAL_MUSHROOM);
        CoralFeature coral = features.get(random.nextInt(features.size()));
        MapColor color = blockUnder.getMapColor(world, pos);
        BlockState properBlock = blockUnder;
        RegistryEntryList.Named<Block> blocks = Registry.BLOCK.getEntryList(BlockTags.CORAL_BLOCKS).orElse(null);
        if (blocks == null) return;
        for (RegistryEntry<Block> block : blocks) {
            properBlock = block.value().getDefaultState();
            if (properBlock.getMapColor(world, pos) == color) {
                break;
            }
        }
        world.setBlockState(pos, Blocks.WATER.getDefaultState(), 4);

        if (!((ExtendedCoralFeature) coral).quickcarpet$growSpecific(world, random, pos, properBlock)) {
            world.setBlockState(pos, blockUnder);
        } else {
            if (world.random.nextInt(4) == 0) {
                BlockPos randomPos = pos.add(world.random.nextInt(16) - 8, world.random.nextInt(8), world.random.nextInt(16) - 8);
                if (world.getBlockState(randomPos).isIn(BlockTags.CORAL_BLOCKS)) {
                    world.setBlockState(randomPos, Blocks.WET_SPONGE.getDefaultState());
                }
            }
        }
    }
}
