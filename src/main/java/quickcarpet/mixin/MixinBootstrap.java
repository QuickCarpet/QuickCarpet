package quickcarpet.mixin;

import net.minecraft.Bootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.feature.DispenserAddons;

@Mixin(Bootstrap.class)
public abstract class MixinBootstrap
{
    @Inject(method = "initialize", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/block/dispenser/DispenserBehavior;registerDefaults()V"))
    private static void registerSpecial(CallbackInfo ci)
    {
        DispenserAddons.registerDefaults();
    }
}
