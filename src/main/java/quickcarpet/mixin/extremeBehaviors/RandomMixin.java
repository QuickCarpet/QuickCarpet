package quickcarpet.mixin.extremeBehaviors;

import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.settings.Settings;

@Mixin(Random.class)
public interface RandomMixin {
    @Shadow double nextDouble();

    @Inject(method = "nextTriangular", at = @At("HEAD"), cancellable = true)
    private void quickcarpet$extremeBehaviors$nextTriangular(double mode, double deviation, CallbackInfoReturnable<Double> cir) {
        if (Settings.extremeBehaviors) {
            cir.setReturnValue(mode + this.nextDouble() * 2 * deviation - deviation);
        }
    }
}
