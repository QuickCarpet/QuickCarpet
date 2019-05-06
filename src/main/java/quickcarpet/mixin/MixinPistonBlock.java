package quickcarpet.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonBlock.class)
public class MixinPistonBlock extends FacingBlock {
    protected MixinPistonBlock(Settings block$Settings_1) {
        super(block$Settings_1);
    }

    @Inject(method = "isMovable", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;hasBlockEntity()Z"), cancellable = true)
    private static void craftingTableMoveable(BlockState state, World woprld, BlockPos pos, Direction pistonDirection, boolean allowDestroy, Direction moveDirection, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof CraftingTableBlock) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
