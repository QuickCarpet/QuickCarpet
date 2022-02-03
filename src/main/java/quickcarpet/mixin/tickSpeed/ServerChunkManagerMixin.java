package quickcarpet.mixin.tickSpeed;

import net.minecraft.server.world.ServerChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.helper.TickSpeed;

@Mixin(ServerChunkManager.class)
public class ServerChunkManagerMixin {
    @Inject(method = "tickChunks", at = @At("HEAD"), cancellable = true)
    private void quickcarpet$tickFreeze$tickChunks(CallbackInfo ci) {
        if (TickSpeed.getServerTickSpeed().isPaused()) {
            ci.cancel();
        }
    }
}
