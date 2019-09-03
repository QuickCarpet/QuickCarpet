package quickcarpet.utils;

import net.minecraft.block.*;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import static quickcarpet.utils.Constants.SetBlockState.NO_FILL_UPDATE;
import static quickcarpet.utils.Constants.SetBlockState.SEND_TO_CLIENT;

public final class BlockRotator {
    private BlockRotator() {}

    public static boolean flipBlock(ItemUsageContext usageContext) {
        World world = usageContext.getWorld();
        BlockPos pos = usageContext.getBlockPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof HorizontalFacingBlock) {
            world.setBlockState(pos, ((HorizontalFacingBlock) block).rotate(state, BlockRotation.CLOCKWISE_90), NO_FILL_UPDATE | SEND_TO_CLIENT);
            return true;
        }
        if (block instanceof ObserverBlock || block instanceof EndRodBlock || block instanceof DispenserBlock || block instanceof PistonBlock) {
            if (block instanceof PistonBlock && state.get(PistonBlock.EXTENDED)) return false;
            world.setBlockState(pos, state.with(FacingBlock.FACING, state.get(FacingBlock.FACING).getOpposite()), NO_FILL_UPDATE | SEND_TO_CLIENT);
            return true;
        }
        if (block instanceof SlabBlock) {
            if (state.get(SlabBlock.TYPE) == SlabType.DOUBLE) return false;
            world.setBlockState(pos, state.with(SlabBlock.TYPE, state.get(SlabBlock.TYPE) == SlabType.TOP ? SlabType.BOTTOM : SlabType.TOP), NO_FILL_UPDATE | SEND_TO_CLIENT);
            return true;
        }
        if (block instanceof HopperBlock) {
            if (state.get(HopperBlock.FACING) == Direction.DOWN) return false;
            world.setBlockState(pos, ((HopperBlock) block).rotate(state, BlockRotation.CLOCKWISE_90), NO_FILL_UPDATE | SEND_TO_CLIENT);
            return true;
        }
        if (block instanceof StairsBlock) {
            Direction side = usageContext.getSide();
            Vec3d hit = usageContext.getHitPos().subtract(pos.getX(), pos.getY(), pos.getZ());
            if ((side == Direction.UP && (hit.y == 1 || hit.y == 0.5)) || (side == Direction.DOWN && (hit.y == 0 || hit.y == 0.5))) {
                world.setBlockState(pos, state.with(StairsBlock.HALF, state.get(StairsBlock.HALF) == BlockHalf.TOP ? BlockHalf.BOTTOM : BlockHalf.TOP ), NO_FILL_UPDATE | SEND_TO_CLIENT);
                return true;
            }
            boolean ccw;
            if (side == Direction.NORTH) ccw = hit.x <= 0.5;
            else if (side == Direction.SOUTH) ccw = hit.x > 0.5;
            else if (side == Direction.EAST) ccw = hit.z <= 0.5;
            else ccw = hit.z > 0.5;

            world.setBlockState(pos, ((StairsBlock) block).rotate(state, ccw ? BlockRotation.COUNTERCLOCKWISE_90 : BlockRotation.CLOCKWISE_90));
            return true;
        }
        return false;
    }
}
