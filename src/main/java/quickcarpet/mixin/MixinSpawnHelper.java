package quickcarpet.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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
import quickcarpet.utils.SpawnTracker;

@Mixin(SpawnHelper.class)
public class MixinSpawnHelper {

    @Redirect(
        method = "spawnEntitiesInChunk",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z")
    )
    private static boolean onSuccessfulSpawn(World world, Entity entity) {
        if (world.spawnEntity(entity)) {
            SpawnTracker.registerSpawn(entity);
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
}
