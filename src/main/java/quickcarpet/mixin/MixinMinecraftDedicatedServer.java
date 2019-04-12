package quickcarpet.mixin;

import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.QuickCarpet;
import quickcarpet.logging.LoggerRegistry;

@Mixin(MinecraftDedicatedServer.class)
public abstract class MixinMinecraftDedicatedServer {

    @Inject(method = "setupServer", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/util/UserCache;setUseRemote(Z)V"))
    private void onSetupServerDedicated(CallbackInfoReturnable<Boolean> cir) {
        QuickCarpet.onServerLoaded((MinecraftDedicatedServer) (Object) this);
        QuickCarpet.onGameStarted();
    }
}
