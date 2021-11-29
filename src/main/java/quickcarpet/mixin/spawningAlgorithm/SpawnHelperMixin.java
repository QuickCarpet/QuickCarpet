package quickcarpet.mixin.spawningAlgorithm;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.settings.Settings;

@Mixin(SpawnHelper.class)
public class SpawnHelperMixin {
    @Redirect(method = "spawnEntitiesInChunk(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/SpawnHelper$Checker;Lnet/minecraft/world/SpawnHelper$Runner;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/SpawnHelper;getRandomPosInChunkSection(Lnet/minecraft/world/World;Lnet/minecraft/world/chunk/WorldChunk;)Lnet/minecraft/util/math/BlockPos;"))
    private static BlockPos quickcarpet$getSpawnPos(World world, WorldChunk chunk) {
        return Settings.spawningAlgorithm.getSpawnPos(world, chunk);
    }
}
