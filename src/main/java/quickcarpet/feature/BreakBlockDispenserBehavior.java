package quickcarpet.feature;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import quickcarpet.settings.Settings;

public class BreakBlockDispenserBehavior extends ItemDispenserBehavior {
    private static final ItemStack SILK_TOUCH_TOOL = new ItemStack(Items.DIAMOND_PICKAXE);

    static {
        SILK_TOUCH_TOOL.addEnchantment(Enchantments.SILK_TOUCH, 1);
    }

    @Override
    public ItemStack dispenseSilently(BlockPointer blockPointer, ItemStack itemStack) {
        switch (Settings.dispensersBreakBlocks) {
            case NORMAL -> {
                if (breakBlock(blockPointer, false)) {
                    spawnParticles(blockPointer, blockPointer.getBlockState().get(DispenserBlock.FACING));
                    itemStack.decrement(1);
                    return itemStack;
                }
            }
            case SILK_TOUCH -> {
                if (breakBlock(blockPointer, true)) {
                    spawnParticles(blockPointer, blockPointer.getBlockState().get(DispenserBlock.FACING));
                    itemStack.decrement(1);
                    return itemStack;
                }
            }
        }
        return super.dispenseSilently(blockPointer, itemStack);
    }

    private boolean breakBlock(BlockPointer blockPointer, boolean silkTouch) {
        ServerWorld world = blockPointer.getWorld();
        Direction facing = blockPointer.getBlockState().get(DispenserBlock.FACING);
        BlockPos target = blockPointer.getPos().offset(facing);
        BlockState state = world.getBlockState(target);
        float hardness = state.getHardness(world, target);
        if (state.isAir() || state.getMaterial().isLiquid() || hardness < 0) return false;
        if (!silkTouch) {
            if (!world.breakBlock(target, true)) return false;
        } else {
            breakBlock(world, target, state, SILK_TOUCH_TOOL);
        }
        spawnParticles(blockPointer, blockPointer.getBlockState().get(DispenserBlock.FACING));
        return true;
    }

    public enum Option {
        FALSE, NORMAL, SILK_TOUCH
    }

    public static void breakBlock(ServerWorld world, BlockPos pos, BlockState blockState, ItemStack tool) {
        LootContext.Builder builder = new LootContext.Builder(world).random(world.random);
        builder.parameter(LootContextParameters.ORIGIN, Vec3d.of(pos)).parameter(LootContextParameters.TOOL, tool);
        blockState.getDroppedStacks(builder).forEach(drop -> {
            Block.dropStack(world, pos, drop);
        });
        blockState.onStacksDropped(world, pos, ItemStack.EMPTY);
        FluidState fluidState = world.getFluidState(pos);
        world.setBlockState(pos, fluidState.getBlockState());
        world.syncWorldEvent(2001, pos, Block.getRawIdFromState(blockState));
    }
}
