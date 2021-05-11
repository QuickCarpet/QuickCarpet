package quickcarpet.mixin.client;

import net.minecraft.client.sound.SoundSystem;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.client.ClientSetting;

@Mixin(SoundSystem.class)
public class SoundSystemMixin {
    @Redirect(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;)V", remap = false))
    private void skipLog(Logger logger, String message) {
        if (ClientSetting.SOUND_ENGINE_FIX.get()) return;
        logger.warn(message);
    }
}
