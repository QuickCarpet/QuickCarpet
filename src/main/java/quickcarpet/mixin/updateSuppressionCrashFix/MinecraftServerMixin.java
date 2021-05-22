package quickcarpet.mixin.updateSuppressionCrashFix;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.crash.CrashException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.settings.Settings;
import quickcarpet.utils.Messenger;
import quickcarpet.utils.PistonHelper;
import quickcarpet.utils.ThrowableUpdateSuppression;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Redirect(method = "tickWorlds", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;tick(Ljava/util/function/BooleanSupplier;)V"))
    private void fixUpdateSuppressionCrashTick(ServerWorld serverWorld, BooleanSupplier shouldKeepTicking) {
        if (!Settings.updateSuppressionCrashFix) {
            serverWorld.tick(shouldKeepTicking);
            return;
        }
        try {
            serverWorld.tick(shouldKeepTicking);
        } catch (CrashException e) {
            if (!(e.getReport().getCause() instanceof ThrowableUpdateSuppression)) throw e;
            logUpdateSuppression("world tick");
        } catch (ThrowableUpdateSuppression ignored) {
            logUpdateSuppression("world tick");
        }
    }

    private void logUpdateSuppression(String phase) {
        PistonHelper.finishPush();
        Messenger.broadcast((MinecraftServer) (Object) this, "You just caused a server crash in " + phase + ".");
    }
}
