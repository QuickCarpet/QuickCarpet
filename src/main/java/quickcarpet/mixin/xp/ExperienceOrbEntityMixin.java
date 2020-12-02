package quickcarpet.mixin.xp;

import net.minecraft.entity.ExperienceOrbEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.settings.Settings;

@Mixin(ExperienceOrbEntity.class)
public class ExperienceOrbEntityMixin {
    @ModifyConstant(method = "onPlayerCollision", constant = @Constant(intValue = 2))
    private int setPickupDelay(int value) {
        return Settings.xpCoolDown;
    }

    @Inject(method = "isMergeable(Lnet/minecraft/entity/ExperienceOrbEntity;II)Z", at = @At("HEAD"), cancellable = true)
    private static void checkMergeDisabled(ExperienceOrbEntity experienceOrbEntity, int seed, int amount, CallbackInfoReturnable<Boolean> cir) {
        if (!Settings.xpMerging) cir.setReturnValue(false);
    }
}
