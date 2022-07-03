package quickcarpet.feature.dispenser;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarvedPumpkinBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class CarvePumpkinBehavior extends FallibleItemDispenserBehavior {
    public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        // Copied from ShearVinesBehavior
        ServerWorld world = pointer.getWorld();
        Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);
        BlockPos target = pointer.getPos().offset(direction);
        BlockState targetState = world.getBlockState(target);

        if (targetState.getBlock() != Blocks.PUMPKIN) {
            this.setSuccess(false);
            return stack;
        }

        Direction direction2 = direction.getAxis() == Direction.Axis.Y ? direction.getOpposite() : direction;
        if(direction2 == Direction.DOWN || direction2 == Direction.UP) {
            direction2 = Direction.NORTH;
        }
        world.setBlockState(target, (BlockState)Blocks.CARVED_PUMPKIN.getDefaultState().with(CarvedPumpkinBlock.FACING, direction2), 11);

        //Spawn Pumpkin Seeds
        ItemEntity itemEntity = new ItemEntity(world, (double)target.getX() + 0.5 + (double)direction2.getOffsetX() * 0.65, (double)target.getY() + 0.1, (double)target.getZ() + 0.5 + (double)direction2.getOffsetZ() * 0.65, new ItemStack(Items.PUMPKIN_SEEDS, 4));
        itemEntity.setVelocity(0.05 * (double)direction2.getOffsetX() + world.random.nextDouble() * 0.02, 0.05, 0.05 * (double)direction2.getOffsetZ() + world.random.nextDouble() * 0.02);
        world.spawnEntity(itemEntity);

        if (stack.damage(1, world.random, null)) stack.setCount(0);
        this.setSuccess(true);

        return stack;

    }
}
