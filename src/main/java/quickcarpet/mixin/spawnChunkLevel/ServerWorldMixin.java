package quickcarpet.mixin.spawnChunkLevel;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import quickcarpet.annotation.Feature;
import quickcarpet.settings.Settings;

import java.util.function.BiFunction;

@Feature("spawnChunkLevel")
@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {
    protected ServerWorldMixin(LevelProperties levelProperties, DimensionType dimensionType, BiFunction<World, Dimension, ChunkManager> chunkManagerProvider, Profiler profiler, boolean isClient) {
        super(levelProperties, dimensionType, chunkManagerProvider, profiler, isClient);
        throw new AbstractMethodError();
    }

    @ModifyConstant(method = "setSpawnPos", constant = @Constant(intValue = 11), require = 2)
    private int adjustSpawnChunkLevel(int level) {
        return Settings.spawnChunkLevel;
    }
}
