package quickcarpet.mixin.spawning;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import quickcarpet.annotation.Feature;
import quickcarpet.utils.extensions.ExtendedWorld;
import quickcarpet.utils.extensions.SpawnEntityCache;

import java.util.HashMap;
import java.util.Map;

@Feature("optimizedSpawning")
@Mixin(World.class)
public abstract class WorldMixin implements ExtendedWorld, SpawnEntityCache {
    private final Map<EntityType<?>, Entity> CACHED_ENTITIES = new HashMap<>();

    @Shadow public abstract BlockState getBlockState(BlockPos blockPos_1);

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Entity> T getCachedEntity(EntityType<T> type) {
        return (T) CACHED_ENTITIES.get(type);
    }

    @Override
    public <T extends Entity> void setCachedEntity(EntityType<T> type, T entity) {
        CACHED_ENTITIES.put(type, entity);
    }
}
