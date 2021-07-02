package quickcarpet.utils;

import net.minecraft.block.*;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import static quickcarpet.utils.Constants.SetBlockState.NO_FILL_UPDATE;
import static quickcarpet.utils.Constants.SetBlockState.SEND_TO_CLIENT;

public final class BlockRotator {
    private BlockRotator() {}

    public static boolean flipBlock(ItemUsageContext usageContext) {
        World world = usageContext.getWorld();
        BlockPos pos = usageContext.getBlockPos();
        BlockState state = world.getBlockState(pos);
        BlockState flipped = getFlippedState(usageContext, pos, state);
        if (flipped == null) return false;
        world.setBlockState(pos, flipped, NO_FILL_UPDATE | SEND_TO_CLIENT);
        return true;
    }

    @Nullable
    private static BlockState getFlippedState(ItemUsageContext usageContext, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        if (block instanceof HorizontalFacingBlock hfBlock) {
            if (block instanceof BedBlock) return null;
            return hfBlock.rotate(state, BlockRotation.CLOCKWISE_90);
        }
        if (block instanceof AbstractRailBlock rail) {
            //noinspection deprecation
            return rail.rotate(state, BlockRotation.CLOCKWISE_90);
        }
        if (block instanceof ObserverBlock || block instanceof EndRodBlock || block instanceof DispenserBlock || block instanceof PistonBlock) {
            if (block instanceof PistonBlock && state.get(PistonBlock.EXTENDED)) return null;
            return state.with(FacingBlock.FACING, state.get(FacingBlock.FACING).getOpposite());
        }
        if (block instanceof SlabBlock) {
            if (state.get(SlabBlock.TYPE) == SlabType.DOUBLE) return null;
            return state.with(SlabBlock.TYPE, state.get(SlabBlock.TYPE) == SlabType.TOP ? SlabType.BOTTOM : SlabType.TOP);
        }
        if (block instanceof HopperBlock hopper) {
            if (state.get(HopperBlock.FACING) == Direction.DOWN) return null;
            return hopper.rotate(state, BlockRotation.CLOCKWISE_90);
        }
        if (state.contains(Properties.AXIS)) {
            int axis = state.get(Properties.AXIS).ordinal();
            return state.with(Properties.AXIS, Direction.Axis.VALUES[(axis + 1) % 3]);
        }
        if (block instanceof StairsBlock stairs) {
            Direction side = usageContext.getSide();
            Vec3d hit = usageContext.getHitPos().subtract(pos.getX(), pos.getY(), pos.getZ());
            if ((side == Direction.UP && (hit.y == 1 || hit.y == 0.5)) || (side == Direction.DOWN && (hit.y == 0 || hit.y == 0.5))) {
                return state.with(StairsBlock.HALF, state.get(StairsBlock.HALF) == BlockHalf.TOP ? BlockHalf.BOTTOM : BlockHalf.TOP);
            }
            boolean ccw;
            if (side == Direction.NORTH) ccw = hit.x <= 0.5;
            else if (side == Direction.SOUTH) ccw = hit.x > 0.5;
            else if (side == Direction.EAST) ccw = hit.z <= 0.5;
            else ccw = hit.z > 0.5;

            return stairs.rotate(state, ccw ? BlockRotation.COUNTERCLOCKWISE_90 : BlockRotation.CLOCKWISE_90);
        }
        return null;
    }
}
