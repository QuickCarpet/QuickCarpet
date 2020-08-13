package quickcarpet.feature;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import quickcarpet.mixin.accessor.AxeItemAccessor;
import quickcarpet.settings.Settings;

public class StripLogsDispenserBehavior extends FallibleItemDispenserBehavior {
    @Override
    protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        if (!Settings.dispensersStripLogs) return super.dispenseSilently(pointer, stack);
        World world = pointer.getWorld();
        Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);
        BlockPos front = pointer.getBlockPos().offset(direction);
        BlockState state = world.getBlockState(front);
        Block block = AxeItemAccessor.getStrippedBlocks().get(state.getBlock());
        if (block == null) {
            this.setSuccess(false);
            return stack;
        }
        world.playSound(null, front, SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1, 1);
        if (stack.damage(1, world.random, null)) stack.setCount(0);
        world.setBlockState(front, block.getDefaultState().with(PillarBlock.AXIS, state.get(PillarBlock.AXIS)));
        this.setSuccess(true);
        return stack;
    }
}
