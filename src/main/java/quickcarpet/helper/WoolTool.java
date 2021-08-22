package quickcarpet.helper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HopperBlock;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;

import static java.util.Map.entry;

public class WoolTool {
    private static final Map<Block, HopperCounter.Key> WOOL_COLORS = Map.ofEntries(
        entry(Blocks.WHITE_WOOL, HopperCounter.Key.WHITE),
        entry(Blocks.ORANGE_WOOL, HopperCounter.Key.ORANGE),
        entry(Blocks.MAGENTA_WOOL, HopperCounter.Key.MAGENTA),
        entry(Blocks.LIGHT_BLUE_WOOL, HopperCounter.Key.LIGHT_BLUE),
        entry(Blocks.YELLOW_WOOL, HopperCounter.Key.YELLOW),
        entry(Blocks.LIME_WOOL, HopperCounter.Key.LIME),
        entry(Blocks.PINK_WOOL, HopperCounter.Key.PINK),
        entry(Blocks.GRAY_WOOL, HopperCounter.Key.GRAY),
        entry(Blocks.LIGHT_GRAY_WOOL, HopperCounter.Key.LIGHT_GRAY),
        entry(Blocks.CYAN_WOOL, HopperCounter.Key.CYAN),
        entry(Blocks.PURPLE_WOOL, HopperCounter.Key.PURPLE),
        entry(Blocks.BLUE_WOOL, HopperCounter.Key.BLUE),
        entry(Blocks.BROWN_WOOL, HopperCounter.Key.BROWN),
        entry(Blocks.GREEN_WOOL, HopperCounter.Key.GREEN),
        entry(Blocks.RED_WOOL, HopperCounter.Key.RED),
        entry(Blocks.BLACK_WOOL, HopperCounter.Key.BLACK)
    );

    public static HopperCounter.Key getCounterKey(World world, BlockPos pos) {
        return WOOL_COLORS.get(world.getBlockState(pos).getBlock());
    }

    public static boolean tryCount(World world, BlockPos pos, BlockState state, Inventory hopper, HopperCounter from) {
        HopperCounter to = HopperCounter.COUNTERS.get(getCounterKey(world, pos.offset(state.get(HopperBlock.FACING))));
        if (to == null) return false;
        for (int i = 0; i < hopper.size(); ++i) {
            if (!hopper.getStack(i).isEmpty()) {
                ItemStack stack = hopper.getStack(i);
                to.add(world.getServer(), stack);
                if (from == null) {
                    hopper.setStack(i, ItemStack.EMPTY);
                } else {
                    from.add(world.getServer(), stack.getItem(), -stack.getCount());
                }
            }
        }
        return true;
    }
}
