package quickcarpet.mixin.waypoints;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import quickcarpet.utils.Waypoint;
import quickcarpet.utils.extensions.WaypointContainer;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements WaypointContainer {
    private Map<String, Waypoint> waypoints = new TreeMap<>();

    protected ServerWorldMixin(LevelProperties levelProperties, DimensionType dimensionType, BiFunction<World, Dimension, ChunkManager> chunkManagerProvider, Profiler profiler, boolean isClient) {
        super(levelProperties, dimensionType, chunkManagerProvider, profiler, isClient);
    }

    @Override
    public Map<String, Waypoint> getWaypoints() {
        return waypoints;
    }

    @Override
    public DimensionType getDimensionType() {
        return dimension.getType();
    }
}
