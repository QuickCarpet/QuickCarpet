package quickcarpet.feature;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.loot.context.LootContext;
import net.minecraft.world.loot.context.LootContextParameters;

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
        FluidState fluidState = world.getFluidState(target);
        LootContext.Builder builder = new LootContext.Builder((ServerWorld) world).setRandom(world.random);
        builder.put(LootContextParameters.POSITION, target);
        builder.put(LootContextParameters.TOOL, stack);
        Block.dropStacks(targetState, builder);
        world.setBlockState(target, fluidState.getBlockState());
        world.playLevelEvent(2001, target, Block.getRawIdFromState(targetState));
        if (stack.damage(1, world.random, null)) stack.setCount(0);
        this.success = true;
        return stack;
    }
}
