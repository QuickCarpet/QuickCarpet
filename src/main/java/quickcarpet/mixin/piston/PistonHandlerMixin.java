package quickcarpet.mixin.piston;

import com.google.common.collect.Lists;
import net.minecraft.block.*;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.ChestType;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.annotation.Feature;
import quickcarpet.settings.Settings;
import quickcarpet.utils.PistonBehaviors;

import java.util.List;


@Mixin(PistonHandler.class)
public abstract class PistonHandlerMixin {
    @Feature("pushLimit")
    @ModifyConstant(method = "tryMove", constant = @Constant(intValue = 12), expect = 3)
    private int adjustPushLimit(int pushLimit) {
        return Settings.pushLimit;
    }



    /*
     * The following Mixins make double chests behave sticky on the side where they are connected to its other double chest half block.
     * This is achieved by Injecting calls to "stickToStickySides" where normally slimeblocks stick to all their neighboring blocks.
     * redirectGetBlockState_1_A/B is neccessary to get access to the blockState_1 variable, which is used in redirectSlimeBlock.
     * redirectSlimeBlock is neccessary to also enable chests to have the backward stickyness (this seems to be an edge case)
     *
     * Note that it is possible to separate chests the same way pistons can separate slimeblocks.
     */
    @Shadow @Final private World world;
    @Shadow protected abstract boolean tryMove(BlockPos blockPos_1, Direction direction_1);
    @Shadow @Final private List<BlockPos> movedBlocks;
    @Shadow @Final private Direction motionDirection;
    @Shadow @Final private List<BlockPos> brokenBlocks;
    @Shadow @Final private BlockPos posFrom;
    //Get access to the blockstate to check if it is a doubleblock later
    private BlockState blockState_1;

    /**
     * Handles blocks besides the slimeblock that are sticky. Currently only supports blocks that are sticky on one side.
     * @author 2No2Name
     */
    @Feature("movableBlockEntities")
    @Inject(method = "tryMove", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;", remap = false, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void stickToStickySides(BlockPos blockPos_1, Direction direction_1, CallbackInfoReturnable<Boolean> cir, BlockState blockState_1, Block block_1, int int_1, int int_2, int int_4, BlockPos blockPos_3, int int_5, int int_6){
        if(!stickToStickySides(blockPos_3)){
            cir.setReturnValue(false);
            cir.cancel();
        }
    }



    /**
     * Handles blocks besides the slimeblock that are sticky. Currently only supports blocks that are sticky on one side.
     * @author 2No2Name
     */
    @Feature("movableBlockEntities")
    @Inject(method = "calculatePush", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;", remap = false, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void stickToStickySides(CallbackInfoReturnable<Boolean> cir, int int_1){
        if(!stickToStickySides(this.movedBlocks.get(int_1))){
            cir.setReturnValue(false);
            cir.cancel();
        }
    }


    @Feature("movableBlockEntities")
    @Redirect(method = "tryMove",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;", ordinal = 0))
    private BlockState redirectGetBlockState_1_A(World world, BlockPos pos) {
        return blockState_1 = world.getBlockState(pos);
    }
    @Feature("movableBlockEntities")
    @Redirect(method = "tryMove",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;", ordinal = 1))
    private BlockState redirectGetBlockState_1_B(World world, BlockPos pos) {
        return blockState_1 = world.getBlockState(pos);
    }


    //Thanks to Earthcomputer for showing how to redirect FIELD access like this
    /**
     * Makes backwards stickyness work with sticky non-slimeblocks as well.
     * @author 2No2Name
     */
    @Feature("movableBlockEntities")
    @Redirect(method = "tryMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/piston/PistonHandler;isBlockSticky(Lnet/minecraft/block/Block;)Z"))
    private boolean modifiedIsSticky(Block block) {
        if (Settings.movableBlockEntities && isStickyOnSide(blockState_1, this.motionDirection.getOpposite())) {
           return true;
        }
        return isBlockSticky(block);
    }

    /**
     * @param blockState blockState of one block
     * @return Direction towards the other block of the double chest, door or bed, null if the blockState is not a double block
     * @author 2No2Name
     */
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
            DoubleBlockHalf half = blockState_1.get(DoorBlock.HALF);
            if (half == DoubleBlockHalf.LOWER)
                return Direction.UP;
            else if (half == DoubleBlockHalf.UPPER)
                return Direction.DOWN;
        }
        if(block instanceof BedBlock){
            return blockState_1.get(BedBlock.PART) == BedPart.FOOT ? blockState_1.get(BedBlock.FACING) : blockState_1.get(BedBlock.FACING).getOpposite();
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
    private boolean isStickyOnSide(BlockState blockState, Direction direction) {
        //Make blocks be sticky on the side to their other half
        return getDirectionToOther(blockState) == direction;
    }

    /**
     * Handles blocks besides the slimeblock that are sticky. Currently only supports blocks that are sticky on one side.
     * Currently the only additional sticky block is the double chest, which sticks to its other chest half.
     * @param blockPos_1 location of a block that moves and needs to stick other blocks to it
     * @author 2No2Name
     */
    private boolean stickToStickySides(BlockPos blockPos_1){
        if(!Settings.movableBlockEntities)
            return true;

        //iterate over several directions to add blocks with multiple sticky sides

        BlockState blockState_1 = this.world.getBlockState(blockPos_1);
        Direction stickyDirection  = getDirectionToOther(blockState_1);

        return stickyDirection == null || this.tryMove(blockPos_1.offset(stickyDirection), stickyDirection);
    }


    /*
     * The following Mixin adds the WEAK_STICKY Piston Behavior
     */

    //weaklyMovedBlocks remembers which blocks are supposed to be moved weakly.
    private final List<BlockPos> weaklyMovedBlocks = Lists.newArrayList();
    //Used to insert the blocks in the correct spot into the moved blocks list
    //lists are updated in parallel, so this is like a "struct of arrays" instead of a "array of structs"
    private final List<BlockPos> weaklyMovedBlocks_moveBefore = Lists.newArrayList();
    private final List<BlockPos> weaklyMovedBlocks_moveBreakOrigin = Lists.newArrayList();
    private final List<BlockPos> weaklyMovedBlocks_moveBreakBefore = Lists.newArrayList();


    @Feature("additionalMovableBlocks")
    @Inject(method = "calculatePush", at = @At(value = "HEAD"))
    private void clearWeaklyMovedBlocks(CallbackInfoReturnable<Boolean> cir){
        weaklyMovedBlocks.clear();
        weaklyMovedBlocks_moveBefore.clear();
        weaklyMovedBlocks_moveBreakOrigin.clear();
        weaklyMovedBlocks_moveBreakBefore.clear();
    }


    @Inject(method = "tryMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;", shift = At.Shift.AFTER, ordinal = 0),locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void doOnlyWeakSticking(BlockPos blockPos_1, Direction arg1, CallbackInfoReturnable<Boolean> cir, BlockState blockState_1, World var13, BlockPos var14){
        if(Settings.movableBlockOverrides){
            PistonBehavior pistonBehavior = blockState_1.getPistonBehavior();
            if ((pistonBehavior == PistonBehaviors.WEAK_STICKY || pistonBehavior == PistonBehaviors.WEAK_STICKY_BREAKABLE) && !movedBlocks.contains(blockPos_1)) {
                //block is being pushed: either move the block or crush it.
                BlockPos brokenBefore = null;
                if(arg1 == this.motionDirection){
                    if(pistonBehavior == PistonBehaviors.WEAK_STICKY_BREAKABLE)
                        if(brokenBlocks.size()>0)
                            brokenBefore = brokenBlocks.get(brokenBlocks.size()-1);
                        else
                            brokenBefore = blockPos_1; //workaround to not use null which means "not breakable"
                    else {
                        //must move, let normal piston deal with it
                        if(weaklyMovedBlocks.contains(blockPos_1)){
                            int i = weaklyMovedBlocks.indexOf(blockPos_1);
                            weaklyMovedBlocks.remove(i);
                            weaklyMovedBlocks_moveBefore.remove(i);
                            weaklyMovedBlocks_moveBreakOrigin.remove(i);
                            weaklyMovedBlocks_moveBreakBefore.remove(i);
                        }
                        return; //no cancel or setReturn value, normal piston code will execute
                    }
                }
                //other cases: (don't break block, just leave it behind when its way is blocked)
                //only add to list when sticking to the side of another block, as weak sticky blocks cannot push more blocks along.
                //block is being pulled, also only add to list
                int i0 = weaklyMovedBlocks.indexOf(blockPos_1);
                if (i0 != -1) {
                    if(brokenBefore != null && weaklyMovedBlocks_moveBreakOrigin.get(i0) == null) {
                        weaklyMovedBlocks_moveBreakOrigin.remove(i0);
                        weaklyMovedBlocks_moveBreakBefore.remove(i0);
                        weaklyMovedBlocks_moveBreakOrigin.add(i0, blockPos_1.offset(arg1.getOpposite()));
                        weaklyMovedBlocks_moveBreakBefore.add(i0,brokenBefore);
                    }
                    cir.setReturnValue(true);
                    return;
                }
                //weaklyMovedBlocks does not already contain blockPos_1
                //Insert directly before the block that would push into blockPos_1 to make sure this block is moved first
                int i1 = weaklyMovedBlocks.indexOf(blockPos_1.offset(this.motionDirection.getOpposite()));
                if(i1 != -1) {
                    weaklyMovedBlocks.add(i1,blockPos_1);
                    weaklyMovedBlocks_moveBefore.add(i1, weaklyMovedBlocks_moveBefore.get(i1));


                    weaklyMovedBlocks_moveBreakOrigin.add(i1,brokenBefore == null ? null : blockPos_1.offset(arg1.getOpposite()));
                    weaklyMovedBlocks_moveBreakBefore.add(i1,brokenBefore);
                    //assert brokenBefore == null; //block cannot be forced pushed if block before was weak sticky

                } else { //Insert at the end if there is no dependency
                    i1 = weaklyMovedBlocks.size();
                    weaklyMovedBlocks.add(blockPos_1);
                    weaklyMovedBlocks_moveBefore.add(blockPos_1.offset(arg1.getOpposite()));
                    weaklyMovedBlocks_moveBreakOrigin.add(brokenBefore == null ? null : blockPos_1.offset(arg1.getOpposite()));
                    weaklyMovedBlocks_moveBreakBefore.add(brokenBefore);
                }

                BlockPos blockPos = blockPos_1.offset(this.motionDirection);
                int j = weaklyMovedBlocks.indexOf(blockPos);
                //reorder in case we just inserted a block in between two blocks
                while(j > i1) {
                    weaklyMovedBlocks_moveBefore.remove(j);
                    weaklyMovedBlocks.remove(j);
                    BlockPos k = weaklyMovedBlocks_moveBreakOrigin.remove(j);
                    BlockPos k2 = weaklyMovedBlocks_moveBreakBefore.remove(j);
                    //assert k == null;
                    //assert k2 == null;

                    //note that add shifts the indices, implicit i++ in the list
                    //Insert directly before the block that would push into blockPos to make sure this block is gone beforehand
                    weaklyMovedBlocks.add(i1,blockPos);
                    weaklyMovedBlocks_moveBefore.add(i1, weaklyMovedBlocks_moveBefore.get(i1));
                    weaklyMovedBlocks_moveBreakOrigin.add(i1,k);//not neccessary to reorder breaking
                    weaklyMovedBlocks_moveBreakBefore.add(i1,k2);//not neccessary to reorder breaking
                    ++i1;

                    blockPos = blockPos_1.offset(this.motionDirection);
                    j = weaklyMovedBlocks.indexOf(blockPos);
                }
                cir.setReturnValue(true);
            }
        }
    }

    @Shadow @Final
    private boolean retracted; //IsExtending

    @Shadow private static boolean isBlockSticky(Block block_1) {
        throw new AbstractMethodError();
    }

    @Inject(method = "calculatePush", at = @At(value = "RETURN", ordinal = 4, shift = At.Shift.BEFORE),locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void decideOnWeaklyStickingBlocks(CallbackInfoReturnable<Boolean> cir){

        if(Settings.movableBlockOverrides) {
            int size = weaklyMovedBlocks.size();
            if(size > 0) {
                int lastIndex = movedBlocks.size();//Debug

                //Iterator<BlockPos> it = weaklyMovedBlocks.iterator();
                //int i = -1;
                //while(it.hasNext()) {
                //for(int i = weaklyMovedBlocks.size()-1; i >=0; i--) {
                for(int i = 0; i < weaklyMovedBlocks.size(); i++) {
                    //i++;
                    BlockPos blockPos = weaklyMovedBlocks.get(i);
                    boolean move = false;

                    BlockPos posDestination = blockPos.offset(this.motionDirection);
                    if(movedBlocks.contains(posDestination)) //intentionally not checking weaklyMovedBlocks.contains
                        move = true;
                        else {
                        BlockState b = world.getBlockState(posDestination);
                        if(b.isAir())
                            move = true;
                        else if(b.getMaterial().isReplaceable()) {
                            move = true;
                            brokenBlocks.add(posDestination);
                        }else if(b.getBlock() == Blocks.PISTON_HEAD && !retracted && posDestination.equals(this.posFrom.offset(this.motionDirection.getOpposite()))){ //is the block in the way the piston head retracting?
                            move = true;
                        }
                    }

                    if(move) {
                        BlockPos successor = weaklyMovedBlocks_moveBefore.get(i);
                        int insertIndex = movedBlocks.indexOf(successor) + 1;

                        assert lastIndex >= insertIndex; //Debug: Ensure movedBlocks is filled from top indicies first, otherwise index shifts may happen
                        lastIndex = insertIndex; //Debug

                        int prevIndex = movedBlocks.indexOf(blockPos);
                        if(prevIndex == -1) {
                            this.movedBlocks.add(insertIndex, blockPos);
                        } else if(insertIndex < prevIndex) {
                            this.movedBlocks.remove(prevIndex);
                            this.movedBlocks.add(insertIndex, blockPos);
                        }
                    } else { //something is in the way of the block
                        //invalidate the entry in weaklyMovedBlocks not neccessary as no contains check happens
                        BlockPos brokenBefore = weaklyMovedBlocks_moveBreakBefore.get(i);
                        if (brokenBefore != null) {
                            int index = brokenBlocks.indexOf(brokenBefore) + 1;
                            this.brokenBlocks.add(index,blockPos);
                        }
                    }
                }
            }
        }
        if(movedBlocks.size() > Settings.pushLimit)
            cir.setReturnValue(false);

    }
}
