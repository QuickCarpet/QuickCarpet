package quickcarpet.mixin.accessor;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.SpawnDensityCapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.Map;

@Mixin(SpawnDensityCapper.class)
public interface SpawnDensityCapperAccessor {
    @Accessor Map<ServerPlayerEntity, SpawnDensityCapper.DensityCap> getPlayersToDensityCap();
    @Invoker List<ServerPlayerEntity> invokeGetMobSpawnablePlayers(ChunkPos chunkPos);
}
