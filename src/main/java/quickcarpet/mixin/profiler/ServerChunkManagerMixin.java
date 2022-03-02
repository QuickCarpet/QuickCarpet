package quickcarpet.mixin.profiler;

import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.utils.CarpetProfiler;

import java.util.function.BooleanSupplier;

@Mixin(ServerChunkManager.class)
public abstract class ServerChunkManagerMixin {
    @Shadow @Final ServerWorld world;

    @Inject(method = "tick(Ljava/util/function/BooleanSupplier;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;tick(Ljava/util/function/BooleanSupplier;)V"))
    private void quickcarpet$profiler$startUnload(BooleanSupplier shouldKeepTicking, boolean tickChunks, CallbackInfo ci) {
        CarpetProfiler.startSection(world, CarpetProfiler.SectionType.CHUNK_UNLOAD);
    }

    @Inject(method = "tick(Ljava/util/function/BooleanSupplier;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;tick(Ljava/util/function/BooleanSupplier;)V", shift = At.Shift.AFTER))
    private void quickcarpet$profiler$endUnload(BooleanSupplier shouldKeepTicking, boolean tickChunks, CallbackInfo ci) {
        CarpetProfiler.endSection(world, CarpetProfiler.SectionType.CHUNK_UNLOAD);
    }
}
