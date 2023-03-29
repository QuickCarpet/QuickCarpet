package quickcarpet.mixin.waypoints;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import quickcarpet.utils.Waypoint;
import quickcarpet.utils.mixin.extensions.WaypointContainer;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements WaypointContainer {
    @Unique private final Map<String, Waypoint> waypoints = new TreeMap<>();

    protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Override
    public Map<String, Waypoint> quickcarpet$getWaypoints() {
        return waypoints;
    }

    @Override
    public RegistryKey<World> quickcarpet$getWaypointWorldKey() {
        return this.getRegistryKey();
    }
}
