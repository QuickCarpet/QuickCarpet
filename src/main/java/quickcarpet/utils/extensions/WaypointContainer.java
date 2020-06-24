package quickcarpet.utils.extensions;

import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import quickcarpet.utils.Waypoint;

import java.util.Map;

public interface WaypointContainer {
    Map<String, Waypoint> getWaypoints();
    RegistryKey<World> getWaypointWorldKey();
    DimensionType getWaypointDimensionType();
}
