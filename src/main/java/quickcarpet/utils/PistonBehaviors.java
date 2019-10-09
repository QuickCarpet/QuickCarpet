package quickcarpet.utils;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import quickcarpet.settings.Settings;

public class PistonBehaviors {
    public static PistonBehavior WEAK_STICKY_BREAKABLE;
    public static PistonBehavior WEAK_STICKY;

    public static boolean isStickyBlock(Block block) {
        return block == Blocks.SLIME_BLOCK || block == Blocks.HONEY_BLOCK;
    }

    public static boolean shouldStickyBlockStick(World world, BlockPos stickyPos, Direction side) {
        if (!Settings.betterHoneyBlock) return true;
        Block selfBlock = world.getBlockState(stickyPos).getBlock();
        Block otherBlock = world.getBlockState(stickyPos.offset(side)).getBlock();
        return !(selfBlock != otherBlock && isStickyBlock(selfBlock) && isStickyBlock(otherBlock));
    }
}
