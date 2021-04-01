package quickcarpet.mixin.updateSuppressionCrashFix;

import net.minecraft.util.thread.ThreadExecutor;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.settings.Settings;
import quickcarpet.utils.ThrowableUpdateSuppression;

@Mixin(ThreadExecutor.class)
public class ThreadExecutorMixin {
    @Redirect(method = "executeTask", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;fatal(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false))
    private void logFatal(Logger logger, String message, Object name, Object t) {
        if (Settings.updateSuppressionCrashFix && (((Throwable) t).getCause() instanceof ThrowableUpdateSuppression)) return;
        logger.fatal(message, name, t);
    }
}
