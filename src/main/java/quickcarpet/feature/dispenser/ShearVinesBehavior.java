package quickcarpet.feature.dispenser;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class ShearVinesBehavior extends FallibleItemDispenserBehavior {
    @Override
    protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        ServerWorld world = pointer.getWorld();
        Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);
        BlockPos target = pointer.getPos().offset(direction);
        BlockState targetState = world.getBlockState(target);
        if (targetState.getBlock() != Blocks.VINE) {
            this.setSuccess(false);
            return stack;
        }
        BreakBlockBehavior.breakBlock(world, target, targetState, stack);
        if (stack.damage(1, world.random, null)) stack.setCount(0);
        this.setSuccess(true);
        return stack;
    }
}
