package quickcarpet.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.settings.Settings;
import quickcarpet.utils.CarpetProfiler;
import quickcarpet.utils.SpawnEntityCache;
import quickcarpet.utils.SpawnTracker;

@Mixin(SpawnHelper.class)
public class SpawnHelperMixin {

    @Redirect(
        method = "spawnEntitiesInChunk",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z")
    )
    private static boolean onSuccessfulSpawn(World world, Entity entity) {
        if (world.spawnEntity(entity)) {
            SpawnTracker.registerSpawn(entity);
            if (Settings.optimizedSpawning) {
                ((SpawnEntityCache) world).setCachedEntity(entity.getType(), null);
            }
            return true;
        }
        return false;
    }

    @Inject(
        method = "spawnEntitiesInChunk",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityType;getCategory()Lnet/minecraft/entity/EntityCategory;"),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void onAttempt(EntityCategory category, World world, WorldChunk chunk, BlockPos spawnPoint, CallbackInfo ci, ChunkGenerator chunkGenerator_1, int mobsSpawned, BlockPos startPos, int x, int y, int z, BlockState state, BlockPos.Mutable blockPos, int pack, int packX, int packZ, int int_8, Biome.SpawnEntry spawnEntry) {
        if (spawnEntry == null) return; // no type selected yet
        Vec3d pos = new Vec3d(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
        SpawnTracker.registerAttempt(world.getDimension().getType(), pos, spawnEntry.type);
    }

    @Redirect(
        method = "spawnEntitiesInChunk",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;doesNotCollide(Lnet/minecraft/util/math/Box;)Z")
    )
    private static boolean doesNotCollide(World world, Box bbox) {
        if (!Settings.optimizedSpawning) return world.doesNotCollide(bbox);
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
                        return world.doesNotCollide(bbox);
                }
            }
        }
        return true;
    }

    @Redirect(method = "spawnEntitiesInChunk", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/EntityType;create(Lnet/minecraft/world/World;)Lnet/minecraft/entity/Entity;"
    ))
    private static Entity create(EntityType type, World world) {
        if (!Settings.optimizedSpawning) return type.create(world);
        Entity cached = ((SpawnEntityCache) world).getCachedEntity(type);
        if (cached != null) return cached;
        cached = type.create(world);
        ((SpawnEntityCache) world).setCachedEntity(type, cached);
        return cached;
    }

    @Inject(
            method = "spawnEntitiesInChunk",
            at = @At("HEAD")
    )
    private static void startSpawning(EntityCategory category, World world, WorldChunk chunk, BlockPos spawnPoint, CallbackInfo ci) {
        CarpetProfiler.startSection(world, CarpetProfiler.SectionType.SPAWNING);
    }

    @Inject(
            method = "spawnEntitiesInChunk",
            at = @At("RETURN")
    )
    private static void endSpawning(EntityCategory category, World world, WorldChunk chunk, BlockPos spawnPoint, CallbackInfo ci) {
        CarpetProfiler.endSection(world);
    }
}
