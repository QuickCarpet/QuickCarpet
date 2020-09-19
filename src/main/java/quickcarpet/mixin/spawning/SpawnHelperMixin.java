package quickcarpet.mixin.spawning;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
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
import quickcarpet.annotation.Feature;
import quickcarpet.helper.Mobcaps;
import quickcarpet.settings.Settings;
import quickcarpet.utils.SpawnTracker;
import quickcarpet.utils.extensions.SpawnEntityCache;

@Mixin(SpawnHelper.class)
public class SpawnHelperMixin {
    @Feature("spawnTracker")
    @Redirect(
            method = "spawnEntitiesInChunk(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/SpawnHelper$Checker;Lnet/minecraft/world/SpawnHelper$Runner;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnEntityAndPassengers(Lnet/minecraft/entity/Entity;)V")
    )
    private static void onSuccessfulSpawn(ServerWorld world, Entity entity) {
        entity.streamPassengersRecursively().forEach(e -> {
            if (world.spawnEntity(e)) {
                SpawnTracker.registerSpawn(entity);
                if (Settings.optimizedSpawning) {
                    ((SpawnEntityCache) world).setCachedEntity(entity.getType(), null);
                }
            }
        });
    }

    @Feature("spawnTracker")
    @Inject(method = "canSpawn(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/world/biome/SpawnSettings$SpawnEntry;Lnet/minecraft/util/math/BlockPos$Mutable;D)Z", at = @At("HEAD"))
    private static void onAttempt(ServerWorld world, SpawnGroup group, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, SpawnSettings.SpawnEntry spawnEntry, BlockPos.Mutable pos, double squaredDistance, CallbackInfoReturnable<Boolean> cir) {
        if (spawnEntry == null) return; // no type selected yet
        Vec3d pos2 = new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        SpawnTracker.registerAttempt(world.getDimension(), pos2, spawnEntry.type);
    }

    @Inject(method = "spawn", at = @At("HEAD"))
    private static void onSpawnStart(ServerWorld world, WorldChunk chunk, SpawnHelper.Info info, boolean spawnAnimals, boolean spawnMonsters, boolean shouldSpawnAnimals, CallbackInfo ci) {
        for (SpawnGroup group : SpawnGroup.values()) {
            SpawnTracker.registerMobcapStatus(world.getDimension(), group, !Mobcaps.isBelowCap(world.getChunkManager(), group));
        }
    }

    @Feature("optimizedSpawning")
    @Redirect(
            method = "canSpawn(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/world/biome/SpawnSettings$SpawnEntry;Lnet/minecraft/util/math/BlockPos$Mutable;D)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;isSpaceEmpty(Lnet/minecraft/util/math/Box;)Z")
    )
    private static boolean doesNotCollide(ServerWorld world, Box bbox) {
        if (!Settings.optimizedSpawning) return world.isSpaceEmpty(bbox);
        BlockPos.Mutable blockpos = new BlockPos.Mutable();
        int minX = MathHelper.floor(bbox.minX);
        int minY = MathHelper.floor(bbox.minY);
        int minZ = MathHelper.floor(bbox.minZ);
        int maxX = MathHelper.floor(bbox.maxX);
        int maxY = MathHelper.floor(bbox.maxY);
        int maxZ = MathHelper.floor(bbox.maxZ);
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    blockpos.set(x, y, z);
                    if (world.getBlockState(blockpos).getCollisionShape(world, blockpos) != VoxelShapes.empty())
                        return world.isSpaceEmpty(bbox);
                }
            }
        }
        return true;
    }

    @Feature("optimizedSpawning")
    @Redirect(method = "createMob", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/EntityType;create(Lnet/minecraft/world/World;)Lnet/minecraft/entity/Entity;"
    ))
    private static <T extends Entity> T create(EntityType<T> type, World world) {
        if (!Settings.optimizedSpawning) return type.create(world);
        T cached = ((SpawnEntityCache) world).getCachedEntity(type);
        if (cached != null) return cached;
        cached = type.create(world);
        ((SpawnEntityCache) world).setCachedEntity(type, cached);
        return cached;
    }
}
