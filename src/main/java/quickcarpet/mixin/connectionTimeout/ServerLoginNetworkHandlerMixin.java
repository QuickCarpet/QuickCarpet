package quickcarpet.mixin.connectionTimeout;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.settings.Settings;

@Mixin(ServerLoginNetworkHandler.class)
public abstract class ServerLoginNetworkHandlerMixin {
    @Shadow public abstract void disconnect(Text reason);

    private long timeoutStart;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void quickcarpet$connectionTimeout$storeStart(MinecraftServer server, ClientConnection connection, CallbackInfo ci) {
        timeoutStart = System.currentTimeMillis();
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue =  600))
    private int quickcarpet$connectionTimeout$disableVanillaTimeout(int tickLimit) {
        return -1;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void quickcarpet$connectionTimeout$fixedTimeout(CallbackInfo ci) {
        if (Settings.connectionTimeout > 0 && System.currentTimeMillis() > timeoutStart + Settings.connectionTimeout * 1000L) {
            this.disconnect(new TranslatableText("multiplayer.disconnect.slow_login"));
        }
    }
}
