package quickcarpet.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.QuickCarpet;
import quickcarpet.QuickCarpetClient;

@Mixin(MinecraftClient.class)
@Environment(EnvType.CLIENT)
public abstract class MinecraftClientMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void quickcarpet$onInit(RunArgs args, CallbackInfo ci) {
        QuickCarpet.getInstance().onGameStarted(EnvType.CLIENT);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void quickcarpet$onTick(CallbackInfo ci) {
        QuickCarpetClient.getInstance().tick();
    }
}
