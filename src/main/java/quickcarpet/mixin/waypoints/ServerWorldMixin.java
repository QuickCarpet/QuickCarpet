package quickcarpet.mixin.waypoints;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import quickcarpet.utils.Waypoint;
import quickcarpet.utils.extensions.WaypointContainer;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements WaypointContainer {
    @Shadow @Nonnull public abstract MinecraftServer getServer();

    @Shadow @Final private MinecraftServer server;
    private Map<String, Waypoint> waypoints = new TreeMap<>();

    protected ServerWorldMixin(MutableWorldProperties mutableWorldProperties, DimensionType dimensionType, Supplier<Profiler> supplier, boolean bl, boolean bl2, long l) {
        super(mutableWorldProperties, dimensionType, supplier, bl, bl2, l);
    }

    @Override
    public Map<String, Waypoint> getWaypoints() {
        return waypoints;
    }

    @Override
    public RegistryKey<DimensionType> getDimensionType() {
        return this.server.method_29174().getRegistry().getKey(getDimension());
    }
}
