package quickcarpet.mixin.extremeBehaviors;

import net.minecraft.block.dispenser.ItemDispenserBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.settings.Settings;

import java.util.Random;

@Mixin(ItemDispenserBehavior.class)
public class ItemDispenserBehaviorMixin {
    @Redirect(method = "spawnItem", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextGaussian()D", remap = false))
    private static double nextGaussian$extremeBehaviors(Random random) {
        return Settings.extremeBehaviors ? random.nextDouble() * 16 - 8 : random.nextGaussian();
    }
}
