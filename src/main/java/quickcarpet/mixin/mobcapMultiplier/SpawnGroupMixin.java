package quickcarpet.mixin.mobcapMultiplier;

import net.minecraft.entity.SpawnGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.settings.Settings;

@Mixin(SpawnGroup.class)
public class SpawnGroupMixin {
    @Inject(method = "getCapacity", at = @At("RETURN"), cancellable = true)
    private void quickcarpet$mobcapMultiplier$getCapacity(CallbackInfoReturnable<Integer> cir) {
        if (Settings.mobcapMultiplier != 1) {
            cir.setReturnValue((int) (cir.getReturnValue() * Settings.mobcapMultiplier));
        }
    }
}
