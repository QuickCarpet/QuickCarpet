package quickcarpet.utils;

import net.minecraft.block.BlockState;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;

public class PistonHelper {
    public static PistonBehavior WEAK_STICKY_BREAKABLE;
    public static PistonBehavior WEAK_STICKY;

    public static PistonBehavior getOverridePistonBehavior(BlockState blockState) {
        if (blockState.isIn(CarpetRegistry.PISTON_OVERRIDE_WEAK_STICKY)) //Adding weak sticky piston behavior
            if (blockState.isIn(CarpetRegistry.PISTON_OVERRIDE_DESTROY))
                return WEAK_STICKY_BREAKABLE;
            else
                return WEAK_STICKY;

        if (blockState.isIn(CarpetRegistry.PISTON_OVERRIDE_MOVABLE))
            return PistonBehavior.NORMAL;

        if (blockState.isIn(CarpetRegistry.PISTON_OVERRIDE_PUSH_ONLY))
            return PistonBehavior.PUSH_ONLY;

        if (blockState.isIn(CarpetRegistry.PISTON_OVERRIDE_IMMOVABLE))
            return PistonBehavior.BLOCK;

        if (blockState.isIn(CarpetRegistry.PISTON_OVERRIDE_DESTROY))
            return PistonBehavior.DESTROY;

        return null;
    }

    public static boolean isBeingPushed(BlockPos pos) {
        Collection<BlockPos> locations = ThreadLocals.movedBlocks.get();
        return locations != null && locations.contains(pos);
    }

    public static void registerPushed(Collection<BlockPos> blocks) {
        ThreadLocals.movedBlocks.set(blocks);
    }

    public static void finishPush() {
        ThreadLocals.movedBlocks.set(null);
    }
}
