package quickcarpet.mixin.loggers;

import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.logging.LogParameter;
import quickcarpet.logging.Loggers;

import java.util.List;

import static quickcarpet.utils.Constants.OtherKeys.*;
import static quickcarpet.utils.Messenger.t;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Inject(
        method = "tickWeather",
        at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/math/intprovider/IntProvider;get(Lnet/minecraft/util/math/random/Random;)I", ordinal = 0),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void quickcarpet$log$weather$thunderOn(CallbackInfo ci, boolean raining, int clearDuration, int thunderDuration) {
        log("thunder", true, thunderDuration);
    }

    @Inject(
        method = "tickWeather",
        at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/math/intprovider/IntProvider;get(Lnet/minecraft/util/math/random/Random;)I", ordinal = 1),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void quickcarpet$log$weather$thunderOff(CallbackInfo ci, boolean raining, int clearDuration, int thunderDuration) {
        log("thunder", false, thunderDuration);
    }

    @Inject(
        method = "tickWeather",
        at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/math/intprovider/IntProvider;get(Lnet/minecraft/util/math/random/Random;)I", ordinal = 2),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void quickcarpet$log$weather$rainOn(CallbackInfo ci, boolean raining, int clearDuration, int thunderDuration, int rainDuration) {
        log("rain", true, rainDuration);
    }

    @Inject(
        method = "tickWeather",
        at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/math/intprovider/IntProvider;get(Lnet/minecraft/util/math/random/Random;)I", ordinal = 3),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void quickcarpet$log$weather$thunderOff(CallbackInfo ci, boolean raining, int clearDuration, int thunderDuration, int rainDuration) {
        log("rain", false, rainDuration);
    }

    private void log(String type, boolean active, int duration) {
        Loggers.WEATHER.log(() ->
                t(WEATHER_LOG_TYPE_PREFIX + type + (active ? WEATHER_LOG_ACTIVE_SUFFIX : WEATHER_LOG_INACTIVE_SUFFIX), duration),
            () -> List.of(
            new LogParameter("type", type),
            new LogParameter("active", active),
            new LogParameter("duration", duration)
        ));
    }
}
