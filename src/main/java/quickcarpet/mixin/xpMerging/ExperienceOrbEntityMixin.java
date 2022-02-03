package quickcarpet.mixin.xpMerging;

import net.minecraft.entity.ExperienceOrbEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.settings.Settings;

@Mixin(ExperienceOrbEntity.class)
public class ExperienceOrbEntityMixin {
    @Inject(method = "isMergeable(Lnet/minecraft/entity/ExperienceOrbEntity;II)Z", at = @At("HEAD"), cancellable = true)
    private static void quickcarpet$xpMerging$checkMergeDisabled(ExperienceOrbEntity experienceOrbEntity, int seed, int amount, CallbackInfoReturnable<Boolean> cir) {
        if (!Settings.xpMerging) cir.setReturnValue(false);
    }
}
