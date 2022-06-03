package quickcarpet.mixin.localDifficulty;

import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.settings.Settings;

@Mixin(LocalDifficulty.class)
public class LocalDifficultyMixin {
    @Inject(method = "setLocalDifficulty", at = @At("HEAD"), cancellable = true)
    private void quickcarpet$localDifficulty(Difficulty difficulty, long timeOfDay, long inhabitedTime, float moonSize, CallbackInfoReturnable<Float> cir) {
        float override = (float) Settings.localDifficulty;
        if (override >= 0) cir.setReturnValue(override);
    }
}
