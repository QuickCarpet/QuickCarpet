package quickcarpet.mixin.profiler;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.annotation.Feature;
import quickcarpet.utils.CarpetProfiler;

import java.util.function.BooleanSupplier;

@Feature("profiler")
@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "tick", at = @At(value = "FIELD", target = "net/minecraft/server/MinecraftServer.ticks:I", shift = At.Shift.AFTER, ordinal = 0))
    private void onTick(BooleanSupplier booleanSupplier_1, CallbackInfo ci) {
        CarpetProfiler.startTick();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void endTick(BooleanSupplier booleanSupplier_1, CallbackInfo ci) {
        CarpetProfiler.endTick((MinecraftServer) (Object) this);
    }

    @Inject(method = "tick", at = @At(value = "CONSTANT", args = "stringValue=save"))
    private void startAutosave(BooleanSupplier booleanSupplier_1, CallbackInfo ci) {
        CarpetProfiler.startSection(null, CarpetProfiler.SectionType.AUTOSAVE);
    }

    @Inject(
            method = "tick",
            slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=save")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/DisableableProfiler;pop()V",
                    ordinal = 0)
    )
    private void endAutosave(BooleanSupplier booleanSupplier_1, CallbackInfo ci) {
        CarpetProfiler.endSection(null);
    }

    @Inject(method = "tickWorlds", at = @At(value = "CONSTANT", args = "stringValue=connection"))
    private void startNetwork(BooleanSupplier booleanSupplier_1, CallbackInfo ci) {
        CarpetProfiler.startSection(null, CarpetProfiler.SectionType.NETWORK);
    }

    @Inject(method = "tickWorlds", at = @At(value = "CONSTANT", args = "stringValue=server gui refresh"))
    private void endNetwork(BooleanSupplier booleanSupplier_1, CallbackInfo ci) {
        CarpetProfiler.endSection(null);
    }
}
