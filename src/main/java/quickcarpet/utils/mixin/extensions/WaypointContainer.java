package quickcarpet.utils.mixin.extensions;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import quickcarpet.utils.Waypoint;

import java.util.Map;

public interface WaypointContainer {
    Map<String, Waypoint> quickcarpet$getWaypoints();
    RegistryKey<World> quickcarpet$getWaypointWorldKey();
}
