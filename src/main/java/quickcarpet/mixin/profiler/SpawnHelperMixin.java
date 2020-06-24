package quickcarpet.mixin.profiler;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.annotation.Feature;
import quickcarpet.utils.CarpetProfiler;

@Feature("profiler")
@Mixin(SpawnHelper.class)
public class SpawnHelperMixin {
    @Inject(method = "spawnEntitiesInChunk(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/SpawnHelper$Checker;Lnet/minecraft/world/SpawnHelper$Runner;)V", at = @At("HEAD"))
    private static void startSpawning(SpawnGroup group, ServerWorld world, WorldChunk chunk, SpawnHelper.Checker checker, SpawnHelper.Runner runner, CallbackInfo ci) {
        CarpetProfiler.startSection(world, CarpetProfiler.SectionType.SPAWNING);
    }

    @Inject(method = "spawnEntitiesInChunk(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/SpawnHelper$Checker;Lnet/minecraft/world/SpawnHelper$Runner;)V", at = @At("RETURN"))
    private static void endSpawning(SpawnGroup group, ServerWorld world, WorldChunk chunk, SpawnHelper.Checker checker, SpawnHelper.Runner runner, CallbackInfo ci) {
        CarpetProfiler.endSection(world);
    }
}
