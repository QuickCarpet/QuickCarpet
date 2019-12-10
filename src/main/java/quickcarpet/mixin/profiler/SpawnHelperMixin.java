package quickcarpet.mixin.profiler;

import net.minecraft.entity.EntityCategory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
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
    @Inject(method = "spawnEntitiesInChunk", at = @At("HEAD"))
    private static void startSpawning(EntityCategory category, ServerWorld world, WorldChunk chunk, BlockPos spawnPoint, CallbackInfo ci) {
        CarpetProfiler.startSection(world, CarpetProfiler.SectionType.SPAWNING);
    }

    @Inject(method = "spawnEntitiesInChunk", at = @At("RETURN"))
    private static void endSpawning(EntityCategory category, ServerWorld world, WorldChunk chunk, BlockPos spawnPoint, CallbackInfo ci) {
        CarpetProfiler.endSection(world);
    }
}
