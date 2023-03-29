package quickcarpet.mixin.core;

import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.QuickCarpet;

@Mixin(Registries.class)
public class RegistriesMixin {
    @Inject(method = "bootstrap", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/Registries;freezeRegistries()V"))
    private static void quickcarpet$onInitialize(CallbackInfo ci) {
        QuickCarpet.getInstance().onRegistryInit();
    }
}
