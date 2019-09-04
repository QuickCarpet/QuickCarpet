package quickcarpet.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.piston.PistonBehavior;

public interface IBlockState {
    static PistonBehavior getOverridePistonBehavior(IBlockState blockState_1){
        Block block = ((BlockState)blockState_1).getBlock();

        if(CarpetRegistry.PISTON_OVERRIDE_WEAK_STICKY.contains(block)) //Adding weak sticky piston behavior
            if(CarpetRegistry.PISTON_OVERRIDE_DESTROY.contains(block))
                return PistonBehaviors.WEAK_STICKY_BREAKABLE;
            else
                return PistonBehaviors.WEAK_STICKY;

        if(CarpetRegistry.PISTON_OVERRIDE_MOVABLE.contains(block))
            return PistonBehavior.NORMAL;

        if(CarpetRegistry.PISTON_OVERRIDE_PUSH_ONLY.contains(block))
            return PistonBehavior.PUSH_ONLY;

        if(CarpetRegistry.PISTON_OVERRIDE_IMMOVABLE.contains(block))
            return PistonBehavior.BLOCK;

        if(CarpetRegistry.PISTON_OVERRIDE_DESTROY.contains(block))
            return PistonBehavior.DESTROY;

        return null;
    }
}