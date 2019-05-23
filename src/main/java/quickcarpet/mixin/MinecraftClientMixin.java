package quickcarpet.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.QuickCarpet;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin
{
    @Inject(method = "init", at = @At(value = "RETURN"))
    private void onInit(CallbackInfo ci)
    {
        QuickCarpet.getInstance().onGameStarted();
    }
}
