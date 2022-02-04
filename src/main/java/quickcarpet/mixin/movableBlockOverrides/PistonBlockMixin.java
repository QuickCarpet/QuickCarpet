package quickcarpet.mixin.movableBlockOverrides;

import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.utils.CarpetRegistry;
import quickcarpet.utils.PistonHelper;

@Mixin(PistonBlock.class)
public class PistonBlockMixin {
    @Inject(method = "isMovable", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;hasBlockEntity()Z"), cancellable = true)
    //Blocks overwritten to be pushable will be pushable without not hasBlockEntity check.
    private static void quickcarpet$movableBlockOverrides$additionalBlocksMovable(BlockState state, World world, BlockPos pos, Direction pistonDirection,
                                                                                  boolean allowDestroy, Direction moveDirection, CallbackInfoReturnable<Boolean> cir) {
        if (quickcarpet.settings.Settings.movableBlockOverrides && CarpetRegistry.PISTON_OVERRIDE_MOVABLE.contains(state.getBlock())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isMovable", at = @At(value = "RETURN", ordinal = 3, shift = At.Shift.BEFORE), cancellable = true)
    private static void quickcarpet$movableBlockOverrides$additionalBlocksMovable2(BlockState blockState_1, World world_1, BlockPos blockPos_1, Direction direction_1,
                                                                                   boolean allowDestroy, Direction direction_2, CallbackInfoReturnable<Boolean> cir) {
        if(quickcarpet.settings.Settings.movableBlockOverrides){
            PistonBehavior override = PistonHelper.getOverridePistonBehavior(blockState_1);
            if(override != null){
                boolean ret = (override == PistonBehavior.NORMAL) ||
                        (override == PistonBehavior.PUSH_ONLY && direction_1 == direction_2) ||
                        (override == PistonBehavior.DESTROY && allowDestroy) ||
                        (override == PistonHelper.WEAK_STICKY) ||
                        (override == PistonHelper.WEAK_STICKY_BREAKABLE);// && allowDestroy);
                cir.setReturnValue(ret);
            }
        }
    }

    @Inject(method = "isMovable", at = @At(value = "RETURN", ordinal = 0, shift = At.Shift.BEFORE), cancellable = true)
    private static void quickcarpet$movableBlockOverrides$additionalObsidianMovable(BlockState blockState_1, World world_1, BlockPos blockPos_1, Direction direction_1, boolean allowDestroy, Direction direction_2, CallbackInfoReturnable<Boolean> cir) {
        if(quickcarpet.settings.Settings.movableBlockOverrides){
            if ((!world_1.getWorldBorder().contains(blockPos_1)) || (blockPos_1.getY() < 0 || direction_1 == Direction.DOWN && blockPos_1.getY() == 0)) {
                return; //return false
            }

            PistonBehavior override = PistonHelper.getOverridePistonBehavior(blockState_1);
            if(override != null){
                boolean ret = (override == PistonBehavior.NORMAL) ||
                        (override == PistonBehavior.PUSH_ONLY && direction_1 == direction_2) ||
                        (override == PistonBehavior.DESTROY && allowDestroy) ||
                        (override == PistonHelper.WEAK_STICKY) ||
                        (override == PistonHelper.WEAK_STICKY_BREAKABLE);// && allowDestroy);
                cir.setReturnValue(ret);
            }
        }
    }

    @Redirect(method = "onSyncedBlockEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getPistonBehavior()Lnet/minecraft/block/piston/PistonBehavior;"))
    private PistonBehavior quickcarpet$movableBlockOverrides$returnNormalWhenMovable(BlockState blockState){
        PistonBehavior pistonBehavior = blockState.getPistonBehavior();
        if(pistonBehavior == PistonHelper.WEAK_STICKY_BREAKABLE || pistonBehavior == PistonHelper.WEAK_STICKY)
            return PistonBehavior.NORMAL;
        return pistonBehavior;
    }
}
