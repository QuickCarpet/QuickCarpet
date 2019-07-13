package quickcarpet.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.QuickCarpet;
import quickcarpet.annotation.BugFix;
import quickcarpet.annotation.Feature;

@Mixin(CommandManager.class)
public abstract class CommandManagerMixin {

    @Shadow @Final private CommandDispatcher<ServerCommandSource> dispatcher;

    @Feature("core")
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onRegister(boolean boolean_1, CallbackInfo ci) {
        QuickCarpet.getInstance().setCommandDispatcher(this.dispatcher);
    }

    @Feature(value = "core", bug = @BugFix(value = "MC-124493", status = "WAI"))
    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;isDebugEnabled()Z"))
    private boolean moreStackTraces(Logger logger) {
        return logger.isDebugEnabled() || QuickCarpet.isDevelopment();
    }

    @Feature(value = "core", bug = @BugFix(value = "MC-124493", status = "WAI"))
    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;isDebugEnabled()Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void printStackTrace(ServerCommandSource source, String command, CallbackInfoReturnable<Integer> cir, Exception e) {
        e.printStackTrace();
    }
}
