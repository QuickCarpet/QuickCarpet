package quickcarpet.mixin;


import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.piston.PistonBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.settings.Settings;
import quickcarpet.utils.CarpetRegistry;
import quickcarpet.utils.IBlockState;

import static net.minecraft.block.piston.PistonBehavior.PUSH_ONLY;


@Mixin(BlockState.class)
public abstract class BlockStateMixin implements IBlockState{

    @Shadow public abstract Block getBlock();

    @Inject(method = "getPistonBehavior", at = @At(value = "INVOKE",target = "Lnet/minecraft/block/Block;getPistonBehavior(Lnet/minecraft/block/BlockState;)Lnet/minecraft/block/piston/PistonBehavior;"), cancellable = true)
    private void getPistonBehavior(CallbackInfoReturnable<PistonBehavior> cir){
        //if(Settings.stickyHoneyBlocks && this.getBlock() == CarpetRegistry.HONEY_BLOCK) {
        //    cir.setReturnValue(PUSH_ONLY);
        //    return;
        //}
        if(Settings.movableBlockOverrides) {
            PistonBehavior pistonBehavior = IBlockState.getOverridePistonBehavior(this);
            if (pistonBehavior != null)
                cir.setReturnValue(pistonBehavior);
        }
    }
}
