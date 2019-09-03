package quickcarpet.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.piston.PistonBehavior;

public interface IBlockState {
    static PistonBehavior getOverwrittenPistonBehavior(IBlockState blockState_1){
        Block block = ((BlockState)blockState_1).getBlock();

        if(CarpetRegistry.PISTON_OVERWRITE_WEAK_STICKY.contains(block)) //Adding weak sticky piston behavior
            if(CarpetRegistry.PISTON_OVERWRITE_DESTROY.contains(block))
                return PistonBehaviors.WEAK_STICKY_BREAKABLE;
            else
                return PistonBehaviors.WEAK_STICKY;

        if(CarpetRegistry.PISTON_OVERWRITE_MOVABLE.contains(block))
            return PistonBehavior.NORMAL;

        if(CarpetRegistry.PISTON_OVERWRITE_PUSH_ONLY.contains(block))
            return PistonBehavior.PUSH_ONLY;

        if(CarpetRegistry.PISTON_OVERWRITE_IMMOVABLE.contains(block))
            return PistonBehavior.BLOCK;

        if(CarpetRegistry.PISTON_OVERWRITE_DESTROY.contains(block))
            return PistonBehavior.DESTROY;

        return null;
    }
}