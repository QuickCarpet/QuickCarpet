package quickcarpet.mixin.movableBlockEntities;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.utils.extensions.ExtendedPistonBlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static quickcarpet.settings.Settings.movableBlockEntities;

@Mixin(PistonBlock.class)
public class PistonBlockMixin extends FacingBlock {
    protected PistonBlockMixin(Settings settings) {
        super(settings);
    }

    //Unnecessary ThreadLocal if client and server use different PistonBlock instances
    @Unique private final ThreadLocal<List<BlockEntity>> movedBlockEntities = new ThreadLocal<>();

    @Inject(method = "isMovable", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;hasBlockEntity()Z"), cancellable = true)
    private static void quickcarpet$movableBlockEntities$craftingTable(BlockState state, World world, BlockPos pos, Direction direction, boolean canBreak, Direction pistonDir, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof CraftingTableBlock) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isMovable", at = @At(value = "RETURN", ordinal = 3, shift = At.Shift.BEFORE), cancellable = true)
    private static void quickcarpet$movableBlockEntities$commandBlock(BlockState state, World world, BlockPos pos, Direction direction, boolean canBreak, Direction pistonDir, CallbackInfoReturnable<Boolean> cir) {
        // Make CommandBlocks movable, either use instanceof CommandBlock or the 3 cmd block objects,
        if (movableBlockEntities && state.getBlock() instanceof CommandBlock) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private static boolean isPushableTileEntityBlock(BlockState state) {
        Block block = state.getBlock();
        // Making PISTON_EXTENSION (BlockPistonMoving) pushable would not work as its createNewTileEntity()-method returns null
        return block != Blocks.ENDER_CHEST && block != Blocks.ENCHANTING_TABLE &&
                block != Blocks.END_GATEWAY && block != Blocks.END_PORTAL && block != Blocks.MOVING_PISTON &&
                block != Blocks.SPAWNER;
    }

    @Redirect(method = "isMovable", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;hasBlockEntity()Z"))
    private static boolean quickcarpet$movableBlockEntities$hasBlockEntity(BlockState state) {
        return state.hasBlockEntity() && (!movableBlockEntities || !isPushableTileEntityBlock(state));
    }

    @Inject(method = "move", at = @At(value = "INVOKE", shift = At.Shift.BEFORE,
            target = "Ljava/util/List;size()I", remap = false, ordinal = 4), locals = LocalCapture.CAPTURE_FAILHARD)
    private void quickcarpet$movableBlockEntities$onMove(World world, BlockPos blockPos_1, Direction direction_1, boolean boolean_1,
                        CallbackInfoReturnable<Boolean> cir, BlockPos blockPos_2, PistonHandler pistonHandler_1,
                        Map<BlockPos, BlockState> map, List<BlockPos> positions, List<BlockState> blockStates) {
        if (!movableBlockEntities) return;
        // Get the blockEntities and remove them from the world before any magic starts to happen
        List<BlockEntity> list = new ArrayList<>();
        for (int i = 0; i < positions.size(); ++i) {
            BlockPos pos = positions.get(i);
            BlockEntity blockEntity = blockStates.get(i).hasBlockEntity() ? world.getBlockEntity(pos) : null;
            list.add(blockEntity);
            if (blockEntity != null) {
                // hopefully this call won't have any side effects in the future, such as dropping all the BlockEntity's items
                // we want to place this same(!) BlockEntity object into the world later when the movement stops again
                world.removeBlockEntity(pos);
                blockEntity.markDirty();
            }
        }
        movedBlockEntities.set(list);
    }

    // A @Redirect would be great here, but it can't capture locals, and we need 'index'
    @Inject(method = "move", at = @At(value = "INVOKE", shift = At.Shift.BEFORE,
            target = "Lnet/minecraft/world/World;addBlockEntity(Lnet/minecraft/block/entity/BlockEntity;)V", ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void quickcarpet$movableBlockEntities$setBlockEntityWithCarried(World world, BlockPos pos, Direction dir, boolean retract, CallbackInfoReturnable<Boolean> cir,
           BlockPos offsetPos, PistonHandler handler, Map<BlockPos, BlockState> blockStateMap, List<BlockPos> movedBlocks, List<BlockState> blockStates, List<BlockPos> brokenBlocks, BlockState[] blockStateArray, Direction movementDirection, int blockStateArrayIndex,
           int index, BlockPos blockEntityPos, BlockState worldState, BlockState movingPistonState) {
        BlockEntity blockEntityPiston = PistonExtensionBlock.createBlockEntityPiston(blockEntityPos, movingPistonState, blockStates.get(index), dir, retract, false);
        if (movableBlockEntities) {
            ((ExtendedPistonBlockEntity) blockEntityPiston).quickcarpet$setCarriedBlockEntity(movedBlockEntities.get().get(index));
        }
        world.addBlockEntity(blockEntityPiston);
    }

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addBlockEntity(Lnet/minecraft/block/entity/BlockEntity;)V", ordinal = 0))
    private void quickcarpet$movableBlockEntities$dontDoAnything(World world, BlockEntity blockEntity) {}

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/PistonExtensionBlock;createBlockEntityPiston(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;ZZ)Lnet/minecraft/block/entity/BlockEntity;", ordinal = 0))
    private BlockEntity quickcarpet$movableBlockEntities$returnNull(BlockPos blockPos, BlockState blockState, BlockState blockState2, Direction direction, boolean bl, boolean bl2) {
        return null;
    }
}
