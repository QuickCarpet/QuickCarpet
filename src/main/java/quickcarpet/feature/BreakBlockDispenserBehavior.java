package quickcarpet.feature;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.loot.context.LootContext;
import net.minecraft.world.loot.context.LootContextParameters;
import quickcarpet.settings.Settings;

public class BreakBlockDispenserBehavior extends ItemDispenserBehavior {
    private static ItemStack SILK_TOUCH_TOOL = new ItemStack(Items.DIAMOND_PICKAXE);

    static {
        SILK_TOUCH_TOOL.addEnchantment(Enchantments.SILK_TOUCH, 1);
    }

    @Override
    public ItemStack dispenseSilently(BlockPointer blockPointer, ItemStack itemStack) {
        switch (Settings.dispensersBreakBlocks) {
            case NORMAL: {
                if (breakBlock(blockPointer, false)) {
                    spawnParticles(blockPointer, blockPointer.getBlockState().get(DispenserBlock.FACING));
                    itemStack.decrement(1);
                    return itemStack;
                }
                break;
            }
            case SILK_TOUCH: {
                if (breakBlock(blockPointer, true)) {
                    spawnParticles(blockPointer, blockPointer.getBlockState().get(DispenserBlock.FACING));
                    itemStack.decrement(1);
                    return itemStack;
                }
                break;
            }
        }
        return super.dispenseSilently(blockPointer, itemStack);
    }

    private boolean breakBlock(BlockPointer blockPointer, boolean silkTouch) {
        ServerWorld world = (ServerWorld) blockPointer.getWorld();
        Direction facing = blockPointer.getBlockState().get(DispenserBlock.FACING);
        BlockPos target = blockPointer.getBlockPos().offset(facing);
        BlockState state = world.getBlockState(target);
        float hardness = state.getHardness(world, target);
        if (state.isAir() || state.getMaterial().isLiquid() || hardness < 0) return false;
        if (!silkTouch) {
            if (!world.breakBlock(target, true)) return false;
        } else {
            LootContext.Builder builder = new LootContext.Builder(world).setRandom(world.random);
            builder.put(LootContextParameters.POSITION, target);
            builder.put(LootContextParameters.TOOL, SILK_TOUCH_TOOL);
            Block.dropStacks(state, builder);
            FluidState fluidState = world.getFluidState(target);
            world.setBlockState(target, fluidState.getBlockState());
            world.playLevelEvent(2001, target, Block.getRawIdFromState(state));
        }
        spawnParticles(blockPointer, blockPointer.getBlockState().get(DispenserBlock.FACING));
        return true;
    }

    public enum Option {
        FALSE, NORMAL, SILK_TOUCH
    }
}
