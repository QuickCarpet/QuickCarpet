package quickcarpet.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.piston.PistonBehavior;

public class PistonBehaviors {
    public static PistonBehavior WEAK_STICKY_BREAKABLE;
    public static PistonBehavior WEAK_STICKY;

    public static PistonBehavior getOverridePistonBehavior(BlockState blockState){
        Block block = blockState.getBlock();

        if(CarpetRegistry.PISTON_OVERRIDE_WEAK_STICKY.contains(block)) //Adding weak sticky piston behavior
            if(CarpetRegistry.PISTON_OVERRIDE_DESTROY.contains(block))
                return WEAK_STICKY_BREAKABLE;
            else
                return WEAK_STICKY;

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
