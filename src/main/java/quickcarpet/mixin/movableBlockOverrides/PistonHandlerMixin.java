package quickcarpet.mixin.movableBlockOverrides;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.settings.Settings;
import quickcarpet.utils.PistonHelper;

import java.util.List;


@Mixin(PistonHandler.class)
public abstract class PistonHandlerMixin {
    @Shadow @Final private World world;
    @Shadow @Final private List<BlockPos> movedBlocks;
    @Shadow @Final private Direction motionDirection;
    @Shadow @Final private List<BlockPos> brokenBlocks;
    @Shadow @Final private BlockPos posFrom;
    @Shadow @Final private boolean retracted;

    /** remembers which blocks are supposed to be moved weakly. */
    private final List<BlockPos> weaklyMovedBlocks = Lists.newArrayList();
    // Used to insert the blocks in the correct spot into the moved blocks list
    // lists are updated in parallel, so this is like a "struct of arrays" instead of an "array of structs"
    private final List<BlockPos> weaklyMovedBlocks_moveBefore = Lists.newArrayList();
    private final List<BlockPos> weaklyMovedBlocks_moveBreakOrigin = Lists.newArrayList();
    private final List<BlockPos> weaklyMovedBlocks_moveBreakBefore = Lists.newArrayList();


    @Inject(method = "calculatePush", at = @At(value = "HEAD"))
    private void quickcarpet$movableBlockOverrides$clearWeaklyMovedBlocks(CallbackInfoReturnable<Boolean> cir){
        weaklyMovedBlocks.clear();
        weaklyMovedBlocks_moveBefore.clear();
        weaklyMovedBlocks_moveBreakOrigin.clear();
        weaklyMovedBlocks_moveBreakBefore.clear();
    }

    @Inject(method = "tryMove", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void quickcarpet$movableBlockOverrides$doOnlyWeakSticking(BlockPos blockPos_1, Direction arg1, CallbackInfoReturnable<Boolean> cir, BlockState blockState_1){
        if (!Settings.movableBlockOverrides) return;
        PistonBehavior pistonBehavior = blockState_1.getPistonBehavior();
        if ((pistonBehavior == PistonHelper.WEAK_STICKY || pistonBehavior == PistonHelper.WEAK_STICKY_BREAKABLE) && !movedBlocks.contains(blockPos_1)) {
            //block is being pushed: either move the block or crush it.
            BlockPos brokenBefore = null;
            if(arg1 == this.motionDirection){
                if(pistonBehavior == PistonHelper.WEAK_STICKY_BREAKABLE)
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

    @Inject(method = "calculatePush", at = @At(value = "RETURN", ordinal = 4), cancellable = true)
    private void quickcarpet$movableBlockOverrides$decideOnWeaklyStickingBlocks(CallbackInfoReturnable<Boolean> cir){
        if (!Settings.movableBlockOverrides) return;
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
        if (movedBlocks.size() > Settings.pushLimit) cir.setReturnValue(false);
    }
}
