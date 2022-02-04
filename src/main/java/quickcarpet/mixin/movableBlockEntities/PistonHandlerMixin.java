package quickcarpet.mixin.movableBlockEntities;

import net.minecraft.block.*;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.ChestType;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.settings.Settings;

import java.util.List;

@Mixin(PistonHandler.class)
public abstract class PistonHandlerMixin {
    @Shadow @Final private World world;
    @Shadow @Final private List<BlockPos> movedBlocks;
    @Shadow @Final private Direction motionDirection;

    @Shadow protected abstract boolean tryMove(BlockPos blockPos_1, Direction direction_1);
    @Shadow private static boolean isBlockSticky(BlockState block_1) {
        throw new AbstractMethodError();
    }

    /*
     * The following Mixins make double chests behave sticky on the side where they are connected to its other double chest half block.
     * This is achieved by Injecting calls to "stickToStickySides" where normally slimeblocks stick to all their neighboring blocks.
     * redirectGetBlockState_1_A/B is neccessary to get access to the blockState_1 variable, which is used in redirectSlimeBlock.
     * redirectSlimeBlock is neccessary to also enable chests to have the backward stickyness (this seems to be an edge case)
     *
     * Note that it is possible to separate chests the same way pistons can separate slimeblocks.
     */

    /**
     * Handles blocks besides the slimeblock that are sticky. Currently only supports blocks that are sticky on one side.
     * @author 2No2Name
     */
    @Inject(method = "tryMove", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;", remap = false, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void quickcarpet$movableBlockEntities$stickToStickySides(BlockPos blockPos_1, Direction direction_1, CallbackInfoReturnable<Boolean> cir, BlockState blockState_1, int int_1, int int_2, int int_4, BlockPos blockPos_3, int int_5, int int_6){
        if(!stickToStickySides(blockPos_3)){
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    /**
     * Handles blocks besides the slimeblock that are sticky. Currently only supports blocks that are sticky on one side.
     * @author 2No2Name
     */
    @Inject(method = "calculatePush", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;", remap = false, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void quickcarpet$movableBlockEntities$stickToStickySides(CallbackInfoReturnable<Boolean> cir, int int_1){
        if(!stickToStickySides(this.movedBlocks.get(int_1))){
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    /**
     * Makes backwards stickyness work with sticky non-slimeblocks as well.
     * @author 2No2Name
     */
    @Redirect(method = "tryMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/piston/PistonHandler;isBlockSticky(Lnet/minecraft/block/BlockState;)Z"))
    private boolean quickcarpet$movableBlockEntities$modifiedIsSticky(BlockState state) {
        if (Settings.movableBlockEntities && isStickyOnSide(state, this.motionDirection.getOpposite())) {
            return true;
        }
        return isBlockSticky(state);
    }

    /**
     * @param blockState blockState of one block
     * @return Direction towards the other block of the double chest, door or bed, null if the blockState is not a double block
     * @author 2No2Name
     */
    @Unique
    private Direction getDirectionToOther(BlockState blockState){
        Block block = blockState.getBlock();
        if(block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST) {
            ChestType chestType;
            try {
                chestType = blockState.get(ChestBlock.CHEST_TYPE);
            } catch (IllegalArgumentException e) {
                return null;
            }
            if (chestType == ChestType.SINGLE)
                return null;
            return ChestBlock.getFacing(blockState);
        }
        if(block instanceof DoorBlock) {
            DoubleBlockHalf half = blockState.get(DoorBlock.HALF);
            if (half == DoubleBlockHalf.LOWER)
                return Direction.UP;
            else if (half == DoubleBlockHalf.UPPER)
                return Direction.DOWN;
        }
        if(block instanceof BedBlock){
            return blockState.get(BedBlock.PART) == BedPart.FOOT ? blockState.get(BedBlock.FACING) : blockState.get(BedBlock.FACING).getOpposite();
        }
        return null;
    }

    /**
     * Returns true if there is a modification making this blockState sticky on the given face. Vanilla stickyness of SLIME_BLOCK is not affected.
     * @param blockState BlockState to determine the stickyness of
     * @param direction Direction in which the stickyness is to be found
     * @return boolean whether block is not SLIME_BLOCK but is sticky in the given direction
     * @author 2No2Name
     */
    @Unique
    private boolean isStickyOnSide(BlockState blockState, Direction direction) {
        //Make blocks be sticky on the side to their other half
        return getDirectionToOther(blockState) == direction;
    }

    /**
     * Handles blocks besides the slimeblock that are sticky. Currently only supports blocks that are sticky on one side.
     * Currently the only additional sticky block is the double chest, which sticks to its other chest half.
     * @param pos location of a block that moves and needs to stick other blocks to it
     * @author 2No2Name
     */
    @Unique
    private boolean stickToStickySides(BlockPos pos){
        if(!Settings.movableBlockEntities)
            return true;

        //iterate over several directions to add blocks with multiple sticky sides

        BlockState state = this.world.getBlockState(pos);
        Direction stickyDirection  = getDirectionToOther(state);

        return stickyDirection == null || this.tryMove(pos.offset(stickyDirection), stickyDirection);
    }
}
