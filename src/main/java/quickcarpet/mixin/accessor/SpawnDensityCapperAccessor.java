package quickcarpet.mixin.accessor;

import net.minecraft.world.SpawnDensityCapper;
import net.minecraft.world.entity.EntityLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.HashMap;
import java.util.List;

@Mixin(SpawnDensityCapper.class)
public interface SpawnDensityCapperAccessor {
    @Accessor HashMap<EntityLike, SpawnDensityCapper.DensityCap> getPlayersToDensityCap();
    @Invoker List<EntityLike> invokeGetMobSpawnablePlayers(long chunkPos);
}
