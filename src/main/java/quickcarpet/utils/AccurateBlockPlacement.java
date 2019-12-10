package quickcarpet.utils;

import net.minecraft.block.*;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.ComparatorMode;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import quickcarpet.mixin.accessor.ItemUsageContextAccessor;
import quickcarpet.settings.Settings;

public class AccurateBlockPlacement {
    public static BlockState getPlacementState(Block block, ItemPlacementContext ctx) {
        if (!Settings.accurateBlockPlacement) return block.getPlacementState(ctx);
        Vec3d hitPos = ctx.getHitPos();
        BlockPos pos = ctx.getBlockPos();
        double hitX = hitPos.x - pos.getX();
        if (hitX < 2) return block.getPlacementState(ctx);
        int code = (int) (hitX - 2) / 2;
        adjustHitX(ctx, hitX % 2);
        return getPlacementStateForCode(block, ctx, code);
    }

    public static BlockState getPlacementStateForCode(Block block, ItemPlacementContext ctx, int code) {
        BlockState state = block.getPlacementState(ctx);
        if (state == null) return null;
        if (block instanceof ObserverBlock) {
            return state
                    .with(ObserverBlock.FACING, Direction.byId(code))
                    .with(ObserverBlock.POWERED, true);
        }
        if (block instanceof RepeaterBlock) {
            int delay = MathHelper.clamp(code >> 4, 1, 4);
            return state
                    .with(RepeaterBlock.FACING, getHorizontalFacing(ctx, code & 0xf))
                    .with(RepeaterBlock.DELAY, delay);
        }
        if (block instanceof TrapdoorBlock) {
            return state
                    .with(TrapdoorBlock.FACING, Direction.byId(code & 0xf))
                    .with(TrapdoorBlock.HALF, code > 0xf ? BlockHalf.TOP : BlockHalf.BOTTOM)
                    .with(TrapdoorBlock.OPEN, ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos()));
        }
        if (block instanceof ComparatorBlock) {
            ComparatorMode mode = code > 0xf ? ComparatorMode.SUBTRACT : ComparatorMode.COMPARE;
            return block.getDefaultState()
                    .with(ComparatorBlock.FACING, getHorizontalFacing(ctx, code & 0xf))
                    .with(ComparatorBlock.MODE, mode);
        }
        if (block instanceof StairsBlock) {
            return state.with(StairsBlock.FACING, Direction.byId(code & 0xf)).with(StairsBlock.HALF, code > 0xf ? BlockHalf.TOP : BlockHalf.BOTTOM);
        }
        if (state.contains(Properties.HORIZONTAL_FACING)) {
            return state.with(Properties.HORIZONTAL_FACING, getHorizontalFacing(ctx, code));
        }
        if (state.contains(Properties.FACING)) {
            return state.with(Properties.FACING, Direction.byId(code));
        }
        return state;
    }

    private static Direction getHorizontalFacing(ItemPlacementContext ctx, int code) {
        Direction facing = Direction.byId(code);
        if (facing.getAxis() == Direction.Axis.Y) return ctx.getPlayer().getHorizontalFacing().getOpposite();
        return facing;
    }

    private static void adjustHitX(ItemUsageContext ctx, double x) {
        BlockHitResult hitResult = ((ItemUsageContextAccessor) ctx).getHitResult();
        Vec3d hitPos = hitResult.getPos();
        hitPos = new Vec3d(x, hitPos.y, hitPos.z);
        if (hitResult.getType() == HitResult.Type.MISS) hitResult = BlockHitResult.createMissed(hitPos, hitResult.getSide(), hitResult.getBlockPos());
        else hitResult = new BlockHitResult(hitPos, hitResult.getSide(), hitResult.getBlockPos(), hitResult.isInsideBlock());
        ((ItemUsageContextAccessor) ctx).setHitResult(hitResult);
    }
}
