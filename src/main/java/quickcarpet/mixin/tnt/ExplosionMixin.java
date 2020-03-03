package quickcarpet.mixin.tnt;

import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.annotation.Feature;
import quickcarpet.settings.Settings;

@Feature("explosionNoBlockDamage")
@Mixin(Explosion.class)
public abstract class ExplosionMixin {
    @Shadow public abstract void clearAffectedBlocks();

    @Inject(method = "affectWorld", at = @At("HEAD"))
    private void explosionNoBlockDamage(boolean bl, CallbackInfo ci) {
        if (Settings.explosionNoBlockDamage) clearAffectedBlocks();
    }
}
