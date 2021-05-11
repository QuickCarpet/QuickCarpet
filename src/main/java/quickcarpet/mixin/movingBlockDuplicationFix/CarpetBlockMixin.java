package quickcarpet.mixin.movingBlockDuplicationFix;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CarpetBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.utils.PistonHelper;

@Mixin(CarpetBlock.class)
public class CarpetBlockMixin extends Block {
    public CarpetBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "getStateForNeighborUpdate", at = @At("HEAD"), cancellable = true)
    private void fixDupe(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom, CallbackInfoReturnable<BlockState> cir) {
        if (quickcarpet.settings.Settings.carpetDuplicationFix && PistonHelper.isBeingPushed(pos)) {
            cir.setReturnValue(super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom));
        }
    }
}
