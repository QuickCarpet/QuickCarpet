package quickcarpet.mixin.piston;


import net.minecraft.block.BlockState;
import net.minecraft.block.piston.PistonBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.settings.Settings;
import quickcarpet.utils.PistonBehaviors;

@Mixin(BlockState.class)
public class BlockStateMixin {
    @Inject(method = "getPistonBehavior", at = @At(value = "INVOKE",target = "Lnet/minecraft/block/Block;getPistonBehavior(Lnet/minecraft/block/BlockState;)Lnet/minecraft/block/piston/PistonBehavior;"), cancellable = true)
    private void getPistonBehavior(CallbackInfoReturnable<PistonBehavior> cir){
        if(Settings.movableBlockOverrides) {
            PistonBehavior pistonBehavior = PistonBehaviors.getOverridePistonBehavior((BlockState) (Object) this);
            if (pistonBehavior != null) cir.setReturnValue(pistonBehavior);
        }
    }
}
