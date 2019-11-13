package quickcarpet.utils;

import net.minecraft.world.dimension.DimensionType;

import java.util.Map;

public interface WaypointContainer {
    Map<String, Waypoint> getWaypoints();
    DimensionType getDimensionType();
}
