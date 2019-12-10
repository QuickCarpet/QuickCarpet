package quickcarpet.mixin.tickSpeed;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.DisableableProfiler;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.QuickCarpet;
import quickcarpet.annotation.Feature;
import quickcarpet.helper.TickSpeed;

import java.util.function.BooleanSupplier;

@Feature("tickSpeed")
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow @Final private static Logger LOGGER;
    @Shadow private volatile boolean running;
    @Shadow private long timeReference;
    @Shadow private long field_4557;
    @Shadow private boolean profilerStartQueued;
    @Shadow @Final private DisableableProfiler profiler;
    @Shadow private volatile boolean loading;
    @Shadow private boolean field_19249;
    @Shadow private long field_19248;

    @Shadow protected abstract boolean shouldKeepTicking();
    @Shadow protected abstract void method_16208();
    @Shadow protected abstract void tick(BooleanSupplier booleanSupplier);

    // Cancel a while statement
    @Redirect(method = "run", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;running:Z"))
    private boolean cancelRunLoop(MinecraftServer server) {
        return false;
    }

    // Replaced the above cancelled while statement with this one
    // could possibly just inject that mspt selection at the beginning of the loop, but then adding all mspt's to
    // replace 50L will be a hassle
    @Inject(method = "run", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/server/MinecraftServer;setFavicon(Lnet/minecraft/server/ServerMetadata;)V"))
    private void modifiedRunLoop(CallbackInfo ci) {
        TickSpeed tickSpeed = QuickCarpet.getInstance().tickSpeed;
        float partialTimeReference = 0;
        while (this.running) {
            //long long_1 = SystemUtil.getMeasuringTimeMs() - this.timeReference;
            //CM deciding on tick speed
            float mspt = 0;
            long behind = 0L;
            if (tickSpeed.tickWarpStartTime != 0 && tickSpeed.continueWarp()) {
                //making sure server won't flop after the warp or if the warp is interrupted
                this.timeReference = this.field_4557 = Util.getMeasuringTimeMs();
            } else {
                mspt = tickSpeed.msptGoal; // regular tick
                behind = Util.getMeasuringTimeMs() - this.timeReference;
            }
            //end tick deciding
            //smoothed out delay to include mspt component. With 50L gives defaults.
            if (behind > /*2000L*/1000L + 20 * mspt && this.timeReference - this.field_4557 >= /*15000L*/10000L + 100 * mspt) {
                float ticks = behind / mspt;//50L;
                LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", behind, ticks);
                this.timeReference += ticks * mspt;//50L;
                this.field_4557 = this.timeReference;
            }

            partialTimeReference += mspt - (long) mspt;
            this.timeReference += (long) mspt;//50L;
            if (partialTimeReference > 1) {
                partialTimeReference--;
                timeReference++;
            }
            if (this.profilerStartQueued) {
                this.profilerStartQueued = false;
                this.profiler.getController().enable();
            }

            this.profiler.startTick();
            this.profiler.push("tick");
            this.tick(this::shouldKeepTicking);
            this.profiler.swap("nextTickWait");
            this.field_19249 = true;
            this.field_19248 = Math.max(Util.getMeasuringTimeMs() + /*50L*/ (long) mspt, this.timeReference);
            this.method_16208();
            this.profiler.pop();
            this.profiler.endTick();
            this.loading = true;
        }

    }

}
