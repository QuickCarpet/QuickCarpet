package quickcarpet.mixin.movableBlockOverrides;


import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.piston.PistonBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.settings.Settings;
import quickcarpet.utils.PistonHelper;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class AbstractBlockStateMixin {
    @Inject(method = "getPistonBehavior", at = @At(value = "INVOKE",target = "Lnet/minecraft/block/Block;getPistonBehavior(Lnet/minecraft/block/BlockState;)Lnet/minecraft/block/piston/PistonBehavior;"), cancellable = true)
    private void quickcarpet$movableBlockOverrides$getPistonBehavior(CallbackInfoReturnable<PistonBehavior> cir){
        if(Settings.movableBlockOverrides) {
            PistonBehavior pistonBehavior = PistonHelper.getOverridePistonBehavior((BlockState) (Object) this);
            if (pistonBehavior != null) cir.setReturnValue(pistonBehavior);
        }
    }
}
