package quickcarpet.mixin.updateSuppressionCrashFix;

import com.mojang.logging.LogUtils;
import net.minecraft.util.thread.ThreadExecutor;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.settings.Settings;
import quickcarpet.utils.ThrowableUpdateSuppression;

@Mixin(ThreadExecutor.class)
public class ThreadExecutorMixin {
    @Redirect(method = "executeTask", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Lorg/slf4j/Marker;Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false))
    private void quickcarpet$updateSuppressionCrashFix$logFatal(Logger logger, Marker marker, String message, Object name, Object t) {
        if (Settings.updateSuppressionCrashFix && (((Throwable) t).getCause() instanceof ThrowableUpdateSuppression)) return;
        logger.error(LogUtils.FATAL_MARKER, message, name, t);
    }
}
