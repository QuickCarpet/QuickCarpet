package quickcarpet.mixin.dustOnPistons;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.block.enums.WireConnection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.settings.Settings;

import java.util.Iterator;

@Mixin(RedstoneWireBlock.class)
public abstract class RedstoneWireBlockMixin {
    @Shadow protected abstract BlockState getPlacementState(BlockView world, BlockState state, BlockPos pos);

    @Redirect(method = "canRunOnTop", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
    private boolean quickcarpet$dustOnPistons$canRunOnPiston(BlockState blockState, Block block) {
        return blockState.isOf(block) || (Settings.dustOnPistons && blockState.getBlock() instanceof PistonBlock);
    }

    @Redirect(method = "canRunOnTop", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isSideSolidFullSquare(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z"))
    private boolean quickcarpet$dustOnPistons$isSideSolidFullSquareOrPiston(BlockState blockState, BlockView world, BlockPos pos, Direction direction) {
        Block block = blockState.getBlock();
        if (Settings.dustOnPistons && block instanceof PistonExtensionBlock) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof PistonBlockEntity pistonBlockEntity) {
                if (pistonBlockEntity.getPushedBlock().getBlock() instanceof PistonBlock) return true;
            }
        }
        return blockState.isSideSolidFullSquare(world, pos, direction);
    }

    private static final ThreadLocal<Direction> lastDirection = new ThreadLocal<>();

    @Inject(method = "getRenderConnectionType(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Z)Lnet/minecraft/block/enums/WireConnection;", at = @At("HEAD"), cancellable = true)
    private void quickcarpet$dustOnPistons$dontConnectOverExtendedPiston(BlockView blockView, BlockPos blockPos, Direction direction, boolean bl, CallbackInfoReturnable<WireConnection> cir) {
        if (!Settings.dustOnPistons) return;
        lastDirection.set(direction);
        BlockState down = blockView.getBlockState(blockPos.down());
        if (down.getBlock() instanceof PistonBlock) {
            boolean extended = down.get(PistonBlock.EXTENDED);
            if (!extended) return;
            Direction pistonFacing = down.get(PistonBlock.FACING);
            if (direction == pistonFacing) {
                cir.setReturnValue(WireConnection.NONE);
            }
        }
    }

    @Redirect(method = "getRenderConnectionType(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Z)Lnet/minecraft/block/enums/WireConnection;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isSolidBlock(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean quickcarpet$dustOnPistons$fakeSolidBlock1(BlockState blockState, BlockView world, BlockPos pos) {
        return fakeSolidBlock(blockState, world, pos);
    }

    @Inject(method = "getReceivedRedstonePower", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;offset(Lnet/minecraft/util/math/Direction;)Lnet/minecraft/util/math/BlockPos;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void quickcarpet$dustOnPistons$rememberDirection(World world, BlockPos pos, CallbackInfoReturnable<Integer> cir, int i, int j, Iterator<Direction> it, Direction direction) {
        lastDirection.set(direction);
    }

    @Redirect(method = "getReceivedRedstonePower", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isSolidBlock(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Z", ordinal = 2))
    private boolean quickcarpet$dustOnPistons$fakeSolidBlock2(BlockState blockState, BlockView world, BlockPos pos) {
        return fakeSolidBlock(blockState, world, pos);
    }

    @Unique
    private boolean fakeSolidBlock(BlockState blockState, BlockView world, BlockPos pos) {
        if (blockState.isSolidBlock(world, pos)) return true;
        if (!Settings.dustOnPistons) return false;
        BlockState wire = world.getBlockState(pos.down());
        if (!wire.isOf(Blocks.REDSTONE_WIRE)) return false;
        BlockState piston = world.getBlockState(pos.down(2));
        if (!(piston.getBlock() instanceof PistonBlock)) return false;
        boolean extended = piston.get(PistonBlock.EXTENDED);
        Direction pistonFacing = piston.get(PistonBlock.FACING);
        return extended && pistonFacing.getOpposite() == lastDirection.get();
    }

    @Inject(method = "getStateForNeighborUpdate", at = @At("HEAD"), cancellable = true)
    private void quickcarpet$dustOnPistons$stateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom, CallbackInfoReturnable<BlockState> cir) {
        if (direction == Direction.DOWN && Settings.dustOnPistons) {
            BlockState updated = getPlacementState(world, state, pos);
            BlockPos.Mutable pos2 = new BlockPos.Mutable();
            for (Direction d : Direction.Type.HORIZONTAL) {
                pos2.set(pos, d).move(Direction.UP);
                BlockState wire = world.getBlockState(pos2);
                if (wire.isOf(Blocks.REDSTONE_WIRE) && world instanceof World) {
                    wire.neighborUpdate((World) world, pos2, (Block) (Object) this, pos, false);
                }
            }
            cir.setReturnValue(updated);
        }
    }
}
