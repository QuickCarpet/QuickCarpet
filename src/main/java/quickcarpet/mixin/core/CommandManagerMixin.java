package quickcarpet.mixin.core;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandException;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
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
import quickcarpet.utils.Translations;

@Mixin(CommandManager.class)
public abstract class CommandManagerMixin {

    @Shadow @Final private CommandDispatcher<ServerCommandSource> dispatcher;

    @Feature("core")
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onRegister(boolean boolean_1, CallbackInfo ci) {
        QuickCarpet.getInstance().setCommandDispatcher(this.dispatcher);
    }

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/SharedConstants;isDevelopment:Z"))
    private boolean registerTest() {
        return true;
    }

    @Feature(value = "core", bug = @BugFix(value = "MC-124493", status = "WAI"))
    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;isDebugEnabled()Z", remap = false))
    private boolean moreStackTraces(Logger logger) {
        return logger.isDebugEnabled() || QuickCarpet.isDevelopment();
    }

    @Feature(value = "core", bug = @BugFix(value = "MC-124493", status = "WAI"))
    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;isDebugEnabled()Z", remap = false), locals = LocalCapture.CAPTURE_FAILHARD)
    private void printStackTrace(ServerCommandSource source, String command, CallbackInfoReturnable<Integer> cir, Exception e) {
        e.printStackTrace();
    }

    @Feature("core")
    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/command/CommandException;getTextMessage()Lnet/minecraft/text/Text;"))
    private Text translateError(CommandException e, ServerCommandSource source, String command) {
        Entity entity = source.getEntity();
        if (entity instanceof ServerPlayerEntity) {
            return Translations.translate(e.getTextMessage(), (ServerPlayerEntity) entity);
        }
        return e.getTextMessage();
    }
}
