package quickcarpet.mixin.core;

import net.minecraft.Bootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.QuickCarpet;

@Mixin(Bootstrap.class)
public class BootstrapMixin {
    @Inject(method = "initialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/Registry;freezeRegistries()V"))
    private static void quickcarpet$onInitialize(CallbackInfo ci) {
        QuickCarpet.getInstance().onBootstrapInitialize();
    }
}
