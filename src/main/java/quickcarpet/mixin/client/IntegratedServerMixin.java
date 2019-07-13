package quickcarpet.mixin.client;

import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.QuickCarpet;
import quickcarpet.annotation.Feature;

@Feature("core")
@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin {

    @Inject(method = "setupServer", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/server/integrated/IntegratedServer;setKeyPair(Ljava/security/KeyPair;)V"))
    private void onSteupServerIntegrated(CallbackInfoReturnable<Boolean> cir) {
        QuickCarpet.getInstance().onServerLoaded((IntegratedServer) (Object) this);
    }
}
