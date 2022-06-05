package quickcarpet.mixin.waypoints;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import quickcarpet.utils.Waypoint;
import quickcarpet.utils.extensions.WaypointContainer;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements WaypointContainer {
    protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> dimension, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, dimension, profiler, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @Unique private final Map<String, Waypoint> waypoints = new TreeMap<>();

    @Override
    public Map<String, Waypoint> quickcarpet$getWaypoints() {
        return waypoints;
    }

    @Override
    public RegistryKey<World> quickcarpet$getWaypointWorldKey() {
        return this.getRegistryKey();
    }
}
