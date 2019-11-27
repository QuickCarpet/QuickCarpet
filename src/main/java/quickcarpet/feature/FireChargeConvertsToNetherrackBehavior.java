package quickcarpet.feature;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class FireChargeConvertsToNetherrackBehavior extends FallibleItemDispenserBehavior {
    @Override
    protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        World world = pointer.getWorld();
        Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);
        BlockPos front = pointer.getBlockPos().offset(direction);
        BlockState state = world.getBlockState(front);
        if (state.getBlock() == Blocks.COBBLESTONE) {
            world.setBlockState(front, Blocks.NETHERRACK.getDefaultState());
            stack.decrement(1);
            this.success = true;
            return stack;
        } else {
            this.success = false;
        }
        return stack;
    }
}
