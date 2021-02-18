package quickcarpet.mixin.client;

import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.QuickCarpet;

@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin {

    @Inject(method = "setupServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/integrated/IntegratedServer;loadWorld()V"))
    private void onSetupServerIntegrated(CallbackInfoReturnable<Boolean> cir) {
        QuickCarpet.getInstance().onServerLoaded((IntegratedServer) (Object) this);
    }
}
