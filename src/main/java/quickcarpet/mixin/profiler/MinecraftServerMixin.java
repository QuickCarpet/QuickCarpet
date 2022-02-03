package quickcarpet.mixin.profiler;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.utils.CarpetProfiler;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "tick", at = @At(value = "FIELD", target = "net/minecraft/server/MinecraftServer.ticks:I", shift = At.Shift.AFTER, ordinal = 0))
    private void quickcarpet$profiler$startTick(BooleanSupplier b, CallbackInfo ci) {
        CarpetProfiler.startTick((MinecraftServer) (Object) this);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void quickcarpet$profiler$endTick(BooleanSupplier b, CallbackInfo ci) {
        CarpetProfiler.endTick((MinecraftServer) (Object) this);
    }

    @Inject(method = "tick", at = @At(value = "CONSTANT", args = "stringValue=Autosave started"))
    private void quickcarpet$profiler$startAutosave(BooleanSupplier b, CallbackInfo ci) {
        CarpetProfiler.startSection(null, CarpetProfiler.SectionType.AUTOSAVE);
    }

    @Inject(method = "tick", at = @At(value = "CONSTANT", args = "stringValue=Autosave finished"))
    private void quickcarpet$profiler$endAutosave(BooleanSupplier b, CallbackInfo ci) {
        CarpetProfiler.endSection(null, CarpetProfiler.SectionType.AUTOSAVE);
    }

    @Inject(method = "tickWorlds", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/function/CommandFunctionManager;tick()V"))
    private void quickcarpet$profiler$startCommandFunctions(BooleanSupplier b, CallbackInfo ci) {
        CarpetProfiler.startSection(null, CarpetProfiler.SectionType.COMMAND_FUNCTIONS);
    }

    @Inject(method = "tickWorlds", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/function/CommandFunctionManager;tick()V", shift = At.Shift.AFTER))
    private void quickcarpet$profiler$endCommandFunctions(BooleanSupplier b, CallbackInfo ci) {
        CarpetProfiler.endSection(null, CarpetProfiler.SectionType.COMMAND_FUNCTIONS);
    }

    @Inject(method = "tickWorlds", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/ServerNetworkIo;tick()V"))
    private void quickcarpet$profiler$startNetwork(BooleanSupplier b, CallbackInfo ci) {
        CarpetProfiler.startSection(null, CarpetProfiler.SectionType.NETWORK);
    }

    @Inject(method = "tickWorlds", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;updatePlayerLatency()V", shift = At.Shift.AFTER))
    private void quickcarpet$profiler$endNetwork(BooleanSupplier b, CallbackInfo ci) {
        CarpetProfiler.endSection(null, CarpetProfiler.SectionType.NETWORK);
    }

    @Inject(method = "runTasksTillTickEnd", at = @At("HEAD"))
    private void quickcarpet$profiler$startTasks(CallbackInfo ci) {
        CarpetProfiler.startSection(null, CarpetProfiler.SectionType.ASYNC_TASKS);
    }

    @Inject(method = "runTasksTillTickEnd", at = @At("RETURN"))
    private void quickcarpet$profiler$endTasks(CallbackInfo ci) {
        CarpetProfiler.endSection(null, CarpetProfiler.SectionType.ASYNC_TASKS);
    }
}
