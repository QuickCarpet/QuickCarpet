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

public class ShearVinesBehavior extends FallibleItemDispenserBehavior {
    @Override
    protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        World world = pointer.getWorld();
        Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);
        BlockPos target = pointer.getBlockPos().offset(direction);
        BlockState targetState = world.getBlockState(target);
        if (targetState.getBlock() != Blocks.VINE) {
            this.success = false;
            return stack;
        }
        BreakBlockDispenserBehavior.breakBlock(world, target, targetState, stack);
        if (stack.damage(1, world.random, null)) stack.setCount(0);
        this.success = true;
        return stack;
    }
}
