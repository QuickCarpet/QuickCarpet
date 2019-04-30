package quickcarpet.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.world.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SpawnRestriction.class)
public abstract class MixinSpawnRestriction
{
    @Shadow
    protected static void register(EntityType<?> entityType_1, SpawnRestriction.Location spawnRestriction$Location_1, Heightmap.Type heightmap$Type_1)
    {
    }
    
    static {
        register(EntityType.SHULKER, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES);
    }
}
