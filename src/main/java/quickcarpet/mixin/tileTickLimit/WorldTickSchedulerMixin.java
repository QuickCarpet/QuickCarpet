package quickcarpet.mixin.tileTickLimit;

import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.tick.WorldTickScheduler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.logging.LogParameter;
import quickcarpet.logging.Loggers;
import quickcarpet.settings.Settings;
import quickcarpet.utils.Messenger;

import java.util.Collections;

@Mixin(WorldTickScheduler.class)
public class WorldTickSchedulerMixin {
    @Unique
    private boolean logged;

    @Inject(method = "collectTickableTicks", at = @At("HEAD"))
    private void resetLogged(long time, int maxTicks, Profiler profiler, CallbackInfo ci) {
        logged = false;
    }

    @Inject(method = "isTickableTicksCountUnder", at = @At("RETURN"))
    private void logTileTickLimit(int maxTicks, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            log();
        }
    }

    @Unique
    private void log() {
        if (logged) return;
        logged = true;
        Loggers.TILE_TICK_LIMIT.log(
            () -> Messenger.t("logger.tileTickLimit.message", Settings.tileTickLimit),
            () -> Collections.singletonList(new LogParameter("LIMIT", Settings.tileTickLimit))
        );
    }
}
