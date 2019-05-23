package quickcarpet.mixin;

import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.QuickCarpet;

@Mixin(MinecraftDedicatedServer.class)
public abstract class MinecraftDedicatedServerMixin {

    @Inject(method = "setupServer", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/util/UserCache;setUseRemote(Z)V"))
    private void onSetupServerDedicated(CallbackInfoReturnable<Boolean> cir) {
        QuickCarpet.getInstance().onServerLoaded((MinecraftDedicatedServer) (Object) this);
        QuickCarpet.getInstance().onGameStarted();
    }
}
