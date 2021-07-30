package quickcarpet.test.mixin;

import net.fabricmc.api.EnvType;
import net.minecraft.test.TestServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.QuickCarpet;

@Mixin(TestServer.class)
public class TestServerMixin {
    @Inject(method = "setupServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/test/TestServer;loadWorld()V"))
    private void onSetupServerDedicated(CallbackInfoReturnable<Boolean> cir) {
        QuickCarpet.getInstance().onGameStarted(EnvType.SERVER);
        QuickCarpet.getInstance().onServerLoaded((TestServer) (Object) this);
    }
}
