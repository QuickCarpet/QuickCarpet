package quickcarpet.utils.extensions;

import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import quickcarpet.utils.Waypoint;

import java.util.Map;

public interface WaypointContainer {
    Map<String, Waypoint> getWaypoints();
    RegistryKey<World> getWaypointWorldKey();
}
