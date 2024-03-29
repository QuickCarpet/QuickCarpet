package quickcarpet.mixin.core;

import net.fabricmc.api.EnvType;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.QuickCarpet;

@Mixin(MinecraftDedicatedServer.class)
public abstract class MinecraftDedicatedServerMixin {

    @Inject(method = "setupServer", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/util/UserCache;setUseRemote(Z)V"))
    private void quickcarpet$onSetupServer(CallbackInfoReturnable<Boolean> cir) {
        QuickCarpet.getInstance().onGameStarted(EnvType.SERVER);
        QuickCarpet.getInstance().onServerLoaded((MinecraftDedicatedServer) (Object) this);
    }
}
