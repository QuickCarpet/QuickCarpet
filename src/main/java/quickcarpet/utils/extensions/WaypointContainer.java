package quickcarpet.utils.extensions;

import net.minecraft.world.dimension.DimensionType;
import quickcarpet.utils.Waypoint;

import java.util.Map;

public interface WaypointContainer {
    Map<String, Waypoint> getWaypoints();
    DimensionType getDimensionType();
}
