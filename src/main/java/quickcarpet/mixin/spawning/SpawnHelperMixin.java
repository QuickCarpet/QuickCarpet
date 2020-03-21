package quickcarpet.mixin.spawning;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.annotation.Feature;
import quickcarpet.settings.Settings;
import quickcarpet.utils.SpawnTracker;
import quickcarpet.utils.extensions.SpawnEntityCache;

@Mixin(SpawnHelper.class)
public class SpawnHelperMixin {
    @Feature("spawnTracker")
    @Redirect(
            method = "method_24930",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnEntity(Lnet/minecraft/entity/Entity;)Z")
    )
    private static boolean onSuccessfulSpawn(ServerWorld world, Entity entity) {
        if (world.spawnEntity(entity)) {
            SpawnTracker.registerSpawn(entity);
            if (Settings.optimizedSpawning) {
                ((SpawnEntityCache) world).setCachedEntity(entity.getType(), null);
            }
            return true;
        }
        return false;
    }

    @Feature("spawnTracker")
    @Inject(method = "method_24934", at = @At("HEAD"))
    private static void onAttempt(ServerWorld world, ChunkGenerator<?> chunkGenerator, Biome.SpawnEntry spawnEntry, BlockPos.Mutable blockPos, double d, CallbackInfoReturnable<Boolean> cir) {
        if (spawnEntry == null) return; // no type selected yet
        Vec3d pos = new Vec3d(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
        SpawnTracker.registerAttempt(world.getDimension().getType(), pos, spawnEntry.type);
    }

    @Feature("optimizedSpawning")
    @Redirect(
            method = "method_24934",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;doesNotCollide(Lnet/minecraft/util/math/Box;)Z")
    )
    private static boolean doesNotCollide(ServerWorld world, Box bbox) {
        if (!Settings.optimizedSpawning) return world.doesNotCollide(bbox);
        BlockPos.Mutable blockpos = new BlockPos.Mutable();
        int minX = MathHelper.floor(bbox.x1);
        int minY = MathHelper.floor(bbox.y1);
        int minZ = MathHelper.floor(bbox.z1);
        int maxX = MathHelper.floor(bbox.x2);
        int maxY = MathHelper.floor(bbox.y2);
        int maxZ = MathHelper.floor(bbox.z2);
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

    @Feature("optimizedSpawning")
    @Redirect(method = "method_24931", at = @At(
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
