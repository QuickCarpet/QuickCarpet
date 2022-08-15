package quickcarpet.mixin.explosionRange;

import net.minecraft.util.math.random.Random;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.settings.Settings;

@Mixin(value = Explosion.class, priority = 1001)
public class ExplosionMixin {
    @Redirect(method = "collectBlocksAndDamageEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/random/Random;nextFloat()F"))
    private float quickcarpet$explosionRange(Random rand) {
        float fixed = (float) Settings.explosionRange;
        return fixed < 0 ? rand.nextFloat() : fixed;
    }

    @Dynamic("lithium mixin")
    @Redirect(method = "performRayCast", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/random/Random;nextFloat()F"), require = 0)
    private float quickcarpet$explosionRange$lithium(Random rand) {
        float fixed = (float) Settings.explosionRange;
        return fixed < 0 ? rand.nextFloat() : fixed;
    }
}
