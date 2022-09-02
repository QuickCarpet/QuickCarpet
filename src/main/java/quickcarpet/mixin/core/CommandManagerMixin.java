package quickcarpet.mixin.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
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
import quickcarpet.api.annotation.BugFix;
import quickcarpet.utils.Translations;

@Mixin(CommandManager.class)
public abstract class CommandManagerMixin {

    @Shadow @Final private CommandDispatcher<ServerCommandSource> dispatcher;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;setConsumer(Lcom/mojang/brigadier/ResultConsumer;)V", remap = false))
    private void quickcarpet$onRegister(CommandManager.RegistrationEnvironment environment, CommandRegistryAccess commandRegistryAccess, CallbackInfo ci) {
        QuickCarpet.getInstance().setCommandDispatcher(this.dispatcher);
    }

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/SharedConstants;isDevelopment:Z"))
    private boolean quickcarpet$registerTest() {
        return true;
    }

    @BugFix(value = "MC-124493", status = "WAI")
    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;isDebugEnabled()Z", remap = false))
    private boolean quickcarpet$moreStackTraces(Logger logger) {
        return logger.isDebugEnabled() || QuickCarpet.isDevelopment();
    }

    @BugFix(value = "MC-124493", status = "WAI")
    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;isDebugEnabled()Z", remap = false), locals = LocalCapture.CAPTURE_FAILHARD)
    private void quickcarpet$printStackTrace(ParseResults<ServerCommandSource> parsed, String command, CallbackInfoReturnable<Integer> cir, ServerCommandSource source, Exception e) {
        e.printStackTrace();
    }

    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/command/CommandException;getTextMessage()Lnet/minecraft/text/Text;"))
    private Text quickcarpet$translateError(CommandException e, ParseResults<ServerCommandSource> parsed, String command) {
        Entity entity = parsed.getContext().getSource().getEntity();
        if (entity instanceof ServerPlayerEntity) {
            return Translations.translate(e.getTextMessage(), (ServerPlayerEntity) entity);
        }
        return e.getTextMessage();
    }
}
