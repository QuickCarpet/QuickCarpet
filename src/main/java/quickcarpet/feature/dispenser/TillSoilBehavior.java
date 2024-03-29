package quickcarpet.feature.dispenser;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import quickcarpet.settings.Settings;

public class TillSoilBehavior extends ItemDispenserBehavior {
    @Override
    protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        if (!Settings.dispensersTillSoil) return super.dispenseSilently(pointer, stack);

        World world = pointer.getWorld();
        Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);
        BlockPos front = pointer.getPos().offset(direction);
        BlockPos down = pointer.getPos().down().offset(direction);
        BlockState frontState = world.getBlockState(front);
        BlockState downState = world.getBlockState(down);

        if (isFarmland(frontState) || isFarmland(downState)) return stack;

        if (canDirectlyTurnToFarmland(frontState)) {
            world.setBlockState(front, Blocks.FARMLAND.getDefaultState());
        } else if (canDirectlyTurnToFarmland(downState)) {
            world.setBlockState(down, Blocks.FARMLAND.getDefaultState());
        } else if (frontState.getBlock() == Blocks.COARSE_DIRT) {
            world.setBlockState(front, Blocks.DIRT.getDefaultState());
        } else if (downState.getBlock() == Blocks.COARSE_DIRT) {
            world.setBlockState(down, Blocks.DIRT.getDefaultState());
        }

        if (stack.damage(1, world.random, null)) stack.setCount(0);

        return stack;
    }

    private boolean canDirectlyTurnToFarmland(BlockState state) {
        return state.getBlock() == Blocks.DIRT || state.getBlock() == Blocks.GRASS_BLOCK || state.getBlock() == Blocks.DIRT_PATH;
    }

    private boolean isFarmland(BlockState state) {
        return state.getBlock() == Blocks.FARMLAND;
    }
}
