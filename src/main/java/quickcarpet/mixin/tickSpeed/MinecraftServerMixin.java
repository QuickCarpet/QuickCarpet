package quickcarpet.mixin.tickSpeed;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.feature.TickSpeed;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow @Final private static Logger LOGGER;
    @Shadow private long timeReference;
    @Shadow private long lastTimeReference;

    @Unique float partialTimeReference = 0;
    @Unique float currentMspt;

    @Redirect(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getMeasuringTimeMs()J", ordinal = 1))
    private long quickcarpet$calculateTickSpeed() {
        TickSpeed tickSpeed = TickSpeed.getServerTickSpeed();
        if (tickSpeed.tickWarpStartTime != 0 && tickSpeed.continueWarp()) {
            this.currentMspt = 0;
            // Make sure the "Can't keep up" doesn't trigger after a warp or if the warp is interrupted
            this.timeReference = this.lastTimeReference = Util.getMeasuringTimeMs();
        } else {
            float mspt = tickSpeed.msptGoal;
            this.currentMspt = mspt;
            this.partialTimeReference += mspt - (long) mspt;
            // Replacement for "Can't keep up" calculation supporting fractional mspt
            long behind = Util.getMeasuringTimeMs() - this.timeReference;
            if (behind > 1000 + 20 * mspt && this.timeReference - this.lastTimeReference >= 1000 + 100 * mspt) {
                float ticks = behind / mspt;
                LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", behind, ticks);
                this.timeReference += ticks * mspt;
                this.lastTimeReference = this.timeReference;
            }
        }
        // Make sure the vanilla "Can't keep up" calculation never triggers
        return this.timeReference;
    }

    @ModifyConstant(method = "runServer", constant = @Constant(longValue = 50))
    private long quickcarpet$tickSpeed$mspt(long constant) {
        return (long) this.currentMspt;
    }

    @Inject(method = "runServer", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/server/MinecraftServer;timeReference:J", ordinal = 2, shift = At.Shift.AFTER))
    private void quickcarpet$tickSpeed$applyPartial(CallbackInfo ci) {
        if (this.partialTimeReference > 1) {
            this.partialTimeReference--;
            this.timeReference++;
        }
    }
}
