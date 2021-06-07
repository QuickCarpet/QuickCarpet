package quickcarpet.mixin.tileTickLimit.compat.lithium;

import net.minecraft.server.world.ServerChunkManager;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.logging.LogParameter;
import quickcarpet.logging.Loggers;
import quickcarpet.settings.Settings;
import quickcarpet.utils.Messenger;

import java.util.Arrays;
import java.util.Iterator;

@Pseudo
@Mixin(targets = "me.jellysquid.mods.lithium.common.world.scheduler.LithiumServerTickScheduler", remap = false)
public class LithiumServerTickSchedulerMixin {
    @Unique
    private boolean logged;

    @ModifyConstant(method = "selectTicks(Lnet/minecraft/server/world/ServerChunkManager;J)V", constant = @Constant(intValue = 65536))
    private int tileTickLimit(int vanilla) {
        logged = false;
        return Settings.tileTickLimit;
    }

    @Inject(method = "selectTicks(Lnet/minecraft/server/world/ServerChunkManager;J)V", at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;add(Ljava/lang/Object;)Z", remap = false, opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT, require = 0)
    private void logTileTickLimit(ServerChunkManager chunkManager, long time, CallbackInfo ci,
                                  long headKey, int limit, boolean canTick, long prevChunk, Iterator<?> it) {
        if (limit == 1 && it.hasNext()) log();
    }

    @Inject(method = "selectTicks(Lnet/minecraft/server/world/ServerChunkManager;J)V",
            at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/lithium/common/world/scheduler/TickEntryQueue;setTickAtIndex(ILme/jellysquid/mods/lithium/common/world/scheduler/TickEntry;)V", remap = false),
            locals = LocalCapture.CAPTURE_FAILSOFT, require = 0)
    private void logTileTickLimit(ServerChunkManager chunkManager, long time, CallbackInfo ci, long headKey, int limit) {
        if (limit <= 0) log();
    }

    @Unique
    private void log() {
        if (logged) return;
        logged = true;
        Loggers.TILE_TICK_LIMIT.log(() -> {
            return Messenger.t("logger.tileTickLimit.message.lithium", Settings.tileTickLimit);
        }, () -> Arrays.asList(
                new LogParameter("LIMIT", Settings.tileTickLimit)
        ));
    }
}