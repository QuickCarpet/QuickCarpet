package quickcarpet.mixin.spawning;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.utils.Mobcaps;
import quickcarpet.utils.SpawnTracker;

@Mixin(SpawnHelper.class)
public class SpawnHelperMixin {
    @Redirect(
        method = "spawnEntitiesInChunk(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/SpawnHelper$Checker;Lnet/minecraft/world/SpawnHelper$Runner;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnEntityAndPassengers(Lnet/minecraft/entity/Entity;)V")
    )
    private static void quickcarpet$onSuccessfulSpawn(ServerWorld world, Entity entity) {
        entity.streamSelfAndPassengers().forEach(e -> {
            if (world.spawnEntity(e)) {
                SpawnTracker.registerSpawn(entity);
            }
        });
    }

    @Inject(method = "canSpawn(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/world/biome/SpawnSettings$SpawnEntry;Lnet/minecraft/util/math/BlockPos$Mutable;D)Z", at = @At("HEAD"))
    private static void quickcarpet$onAttempt(ServerWorld world, SpawnGroup group, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, SpawnSettings.SpawnEntry spawnEntry, BlockPos.Mutable pos, double squaredDistance, CallbackInfoReturnable<Boolean> cir) {
        if (spawnEntry == null) return; // no type selected yet
        Vec3d pos2 = new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        SpawnTracker.registerAttempt(world.getDimension(), pos2, spawnEntry.type);
    }

    @Inject(method = "spawn", at = @At("HEAD"))
    private static void quickcarpet$onSpawnStart(ServerWorld world, WorldChunk chunk, SpawnHelper.Info info, boolean spawnAnimals, boolean spawnMonsters, boolean shouldSpawnAnimals, CallbackInfo ci) {
        for (SpawnGroup group : SpawnGroup.values()) {
            SpawnTracker.registerMobcapStatus(world.getDimension(), group, !Mobcaps.isBelowCap(world.getChunkManager(), group));
        }
    }
}
