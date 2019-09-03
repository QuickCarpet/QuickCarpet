package quickcarpet.mixin;


import net.minecraft.block.BlockState;
import net.minecraft.block.piston.PistonBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.settings.Settings;
import quickcarpet.utils.IBlockState;


@Mixin(BlockState.class)
public class BlockStateMixin implements IBlockState{

    @Inject(method = "getPistonBehavior", at = @At(value = "INVOKE",target = "Lnet/minecraft/block/Block;getPistonBehavior(Lnet/minecraft/block/BlockState;)Lnet/minecraft/block/piston/PistonBehavior;"), cancellable = true)
    private void getPistonBehavior(CallbackInfoReturnable<PistonBehavior> cir){
        if(Settings.movableBlockOverwrites) {
            PistonBehavior pistonBehavior = IBlockState.getOverwrittenPistonBehavior(this);
            if (pistonBehavior != null)
                cir.setReturnValue(pistonBehavior);
        }
    }
}
