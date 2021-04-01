package quickcarpet.mixin.client;

import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.client.ClientSetting;

@Mixin(targets = "net.minecraft.client.sound.SoundEngine$SourceSetImpl")
public class SoundEngineSourceSetImplMixin {
    @Redirect(method = "createSource()Lnet/minecraft/client/sound/Source;", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V", remap = false))
    private void skipLog(Logger logger, String message, Object arg) {
        if (ClientSetting.SOUND_ENGINE_FIX.get()) return;
        logger.warn(message, arg);
    }
}
